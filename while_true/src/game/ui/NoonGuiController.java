package game.ui;

import game.stage.noon.NoonGameLogic;
import game.ui.gameover.GameOverOverlay; // 🔥 게임오버 오버레이 import

/**
 * NoonGuiController
 *
 * [역할]
 * - 점심 스테이지 UI와 GameLogic을 연결하는 컨트롤러
 * - 버튼 입력 → 로직 처리 → 화면 갱신 담당
 *
 * [현재 상태]
 * - 3번 버튼은 임시로 GameOver 연출 테스트용으로 사용 중
 * - 아침 스테이지 완성 후 원래 로직(onUserChoice)으로 복구 예정
 */
public class NoonGuiController {

    private final NoonWindow window;
    private final NoonGameLogic logic;

    public NoonGuiController() {
        window = new NoonWindow();
        logic  = new NoonGameLogic();

        bindEvents();

        // 1) 게임 시작 시 첫 대사 출력
        String firstText = logic.start();
        window.printDialogue(firstText);

        // 2) 시작 상태 표시
        window.setStatusText("현재 상태 → 체력 5 / 멘탈 5 / 지식 5 / 사교 0");

        // 3) 시작 NPC = 1번
        window.setNpcImage(1);

        window.setVisible(true);
    }

    private void bindEvents() {
        window.getBtn1().addActionListener(e -> onUserChoice(1));
        window.getBtn2().addActionListener(e -> onUserChoice(2));
        window.getBtn3().addActionListener(e -> onUserChoice(3));
    }

    /**
     * 사용자의 선택 처리
     */
    private void onUserChoice(int choice) {
        // 1️⃣ 로직 처리 → 전체 텍스트 반환
        String fullText = logic.handleChoice(choice);

        // 2️⃣ 가운데 대사 전체 갱신
        window.printDialogue(fullText);

        // 3️⃣ 변화 로그 + 현재 상태 부분만 왼쪽에 표시
        updateStatusArea(fullText);

        // ★ 4️⃣ Game Over 판정 (여기가 핵심 위치)
        if (logic.isGameOver()) {
            new GameOverOverlay(window).setVisible(true);
            return; // ⚠️ 아래 코드 실행 막음
        }

        // 5️⃣ 대화 회차 → NPC 이미지 변경
        int npcIndex = extractCurrentNpcIndex(fullText);
        if (npcIndex >= 1 && npcIndex <= 12) {
            window.setNpcImage(npcIndex);
        }
    }


    /**
     * fullText 안에서 마지막 "---------- [대화 N회차] ----------" 의 N을 찾아 리턴
     */
    private int extractCurrentNpcIndex(String text) {
        String marker = "---------- [대화 ";
        int idx = text.lastIndexOf(marker);

        if (idx < 0) return -1;

        int start = idx + marker.length();
        int end = text.indexOf("회차", start);

        if (end < 0) return -1;

        try {
            String numStr = text.substring(start, end).trim();
            return Integer.parseInt(numStr);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 변화 로그 부분만 따로 왼쪽 statusArea에 표시
     */
    private void updateStatusArea(String fullText) {
        String statusPart = "";

        int idx = fullText.indexOf("[변화 로그]");
        if (idx >= 0) {
            statusPart = fullText.substring(idx);
            int nextIdx = statusPart.indexOf("---------- [대화");
            if (nextIdx > 0) {
                statusPart = statusPart.substring(0, nextIdx);
            }
        }

        window.setStatusText(statusPart);
    }
}
