package game.ui;

import game.stage.noon.NoonGameLogic;
import game.ui.gameover.GameOverOverlay;

import javax.swing.JOptionPane;

/**
 * NoonGuiController
 *
 * - 버튼 입력 → 로직 처리 → 화면 갱신 담당
 * - 어떤 예외가 나도 게임이 "멈춰 보이는" 상황을 방지하기 위해
 *   안전 래퍼(onUserChoiceSafe)로 전부 감쌈.
 *
 * - 오류가 나면:
 *   1) 콘솔에 STEP 로그 + 스택트레이스 출력
 *   2) 팝업으로 예외 타입/메시지/마지막 단계 표시
 */
public class NoonGuiController {

    private final NoonWindow window;
    private final NoonGameLogic logic;

    // 디버그용: 마지막으로 성공한 단계
    private String lastStep = "(none)";

    public NoonGuiController() {
        window = new NoonWindow();
        logic  = new NoonGameLogic();

        bindEvents();

        // 시작 텍스트도 null 방어
        String firstText = safeString(logic.start());
        window.printDialogue(firstText);

        window.setStatusText("현재 상태 → 체력 5 / 멘탈 5 / 지식 5 / 사교 0");

        // 시작 NPC = 1번
        window.setNpcImage(1);

        window.setVisible(true);
    }

    private void bindEvents() {
        window.getBtn1().addActionListener(e -> onUserChoiceSafe(1));
        window.getBtn2().addActionListener(e -> onUserChoiceSafe(2));
        window.getBtn3().addActionListener(e -> onUserChoiceSafe(3));
    }

    /**
     * ✅ 절대 안 죽게 감싸는 래퍼
     * - 여기서 잡히면 "컨트롤러/로직" 어디든 문제인 건 확실함
     * - 팝업으로 예외 타입/메시지/마지막 단계까지 보여줌
     */
    private void onUserChoiceSafe(int choice) {
        try {
            onUserChoice(choice);
        } catch (Throwable t) {
            String msg = t.getClass().getName() + " : " + String.valueOf(t.getMessage());

            System.err.println("\n==============================");
            System.err.println("[NoonGuiController] ERROR!");
            System.err.println("choice=" + choice);
            System.err.println("lastStep=" + lastStep);
            System.err.println("exception=" + msg);
            System.err.println("==============================\n");
            t.printStackTrace();

            JOptionPane.showMessageDialog(
                    window,
                    "에러 발생!\n\n"
                            + "choice = " + choice + "\n"
                            + "lastStep = " + lastStep + "\n\n"
                            + msg + "\n\n"
                            + "(콘솔 로그도 확인해줘)",
                    "Noon Error",
                    JOptionPane.ERROR_MESSAGE
            );

            // 불친절 감성 유지: 멈추지 않고 최소 메시지만
            window.printDialogue("…뭔가 어긋났다.\n(콘솔/팝업을 확인해봐)");
        }
    }

    /**
     * 실제 처리 (여기서 터지면 Safe가 잡아줌)
     */
    private void onUserChoice(int choice) {
        step("STEP 1) clicked: " + choice);

        // 1) 로직 처리
        step("STEP 2) call logic.handleChoice()");
        String fullText = logic.handleChoice(choice);
        step("STEP 3) handleChoice returned (" + (fullText == null ? "null" : ("len=" + fullText.length())) + ")");

        // null 방어
        fullText = safeString(fullText);

        // 2) 가운데 대사 전체 갱신
        step("STEP 4) window.printDialogue()");
        window.printDialogue(fullText);

        // 3) 변화 로그 표시
        step("STEP 5) updateStatusAreaSafe()");
        updateStatusAreaSafe(fullText);

        // 4) Game Over 판정
        step("STEP 6) logic.isGameOver()");
        if (logic.isGameOver()) {
            step("STEP 7) show GameOverOverlay()");
            new GameOverOverlay(window).setVisible(true);
            return;
        }

        // 5) NPC 이미지 변경 (파싱 실패해도 게임은 진행)
        step("STEP 8) extract npcIndex");
        int npcIndex = extractCurrentNpcIndexSafe(fullText);
        step("STEP 9) npcIndex = " + npcIndex);

        if (npcIndex >= 1 && npcIndex <= 12) {
            step("STEP 10) window.setNpcImage(" + npcIndex + ")");
            window.setNpcImage(npcIndex);
        } else {
            // 파싱 실패해도 죽지 않게
            System.err.println("[NoonGuiController] npcIndex 파싱 실패/범위 밖: " + npcIndex);
        }

        step("STEP 11) done");
    }

    private void step(String s) {
        lastStep = s;
        System.out.println(s);
    }

    private String safeString(String s) {
        return (s == null) ? "" : s;
    }

    /**
     * fullText 안에서 마지막 "---------- [대화 N회차] ----------" 의 N을 찾아 리턴
     * - 포맷이 살짝 달라도 죽지 않게 안전 파싱
     */
    private int extractCurrentNpcIndexSafe(String text) {
        try {
            if (text == null || text.isEmpty()) return -1;

            String marker = "---------- [대화 ";
            int idx = text.lastIndexOf(marker);
            if (idx < 0) return -1;

            int start = idx + marker.length();
            int end = text.indexOf("회차", start);
            if (end < 0) return -1;

            String numStr = text.substring(start, end).trim();
            return Integer.parseInt(numStr);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 변화 로그 부분만 왼쪽 statusArea에 표시
     * - "[변화 로그]"가 없으면 기본 문구만 표시
     */
    private void updateStatusAreaSafe(String fullText) {
        try {
            if (fullText == null || fullText.isEmpty()) {
                window.setStatusText("…");
                return;
            }

            String statusPart = "";

            int idx = fullText.indexOf("[변화 로그]");
            if (idx >= 0) {
                statusPart = fullText.substring(idx);

                int nextIdx = statusPart.indexOf("---------- [대화");
                if (nextIdx > 0) {
                    statusPart = statusPart.substring(0, nextIdx);
                }
            } else {
                statusPart = "…";
            }

            window.setStatusText(statusPart);
        } catch (Exception e) {
            window.setStatusText("…");
        }
    }
}
