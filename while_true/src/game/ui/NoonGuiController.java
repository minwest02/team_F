package game.ui;

import game.stage.noon.NoonGameLogic;
import game.ui.gameover.GameOverOverlay;
import game.core.GameOverReason;

import javax.swing.SwingUtilities;

public class NoonGuiController {

    private final NoonWindow window;
    private final NoonGameLogic logic;

    // ✅ 점심 클리어 시 실행할 콜백(저녁 스테이지 시작용)
    private final Runnable onClear;

    public NoonGuiController() {
        this(null);
    }

    public NoonGuiController(Runnable onClear) {
        this.onClear = (onClear != null) ? onClear : () -> {};

        window = new NoonWindow();
        logic  = new NoonGameLogic();

        bindEvents();

        String firstText = logic.start();
        window.printDialogue(firstText);

        window.setStatusText("현재 상태 → 체력 5 / 멘탈 5 / 지식 5 / 사교 0");
        window.setNpcImage(1);

        window.setVisible(true);

        // ✅ 점심 스테이지 도움말: 시작 시 1회만 표시
        SwingUtilities.invokeLater(() -> GameHelpOverlay_Noon.showOnce(window));
    }

    private void bindEvents() {
        window.getBtn1().addActionListener(e -> onUserChoice(1));
        window.getBtn2().addActionListener(e -> onUserChoice(2));
        window.getBtn3().addActionListener(e -> onUserChoice(3));
    }

    private void onUserChoice(int choice) {
        String fullText = logic.handleChoice(choice);

        window.printDialogue(fullText);
        updateStatusArea(fullText);

        int npcIndex = extractCurrentNpcIndex(fullText);
        if (npcIndex >= 1 && npcIndex <= 12) {
            window.setNpcImage(npcIndex);
        }

        // ✅ GameOver 처리 (오버레이 + 버튼 잠금)
        if (logic.isGameOver()) {
            window.setButtonsEnabled(false);

            // ✅ "Game Over - ~ 0" 문장으로만 판별
            GameOverReason reason = GameOverReason.UNKNOWN;
            if (fullText.contains("Game Over - 멘탈 0")) {
                reason = GameOverReason.MENTAL_ZERO;
            } else if (fullText.contains("Game Over - 체력 0")) {
                reason = GameOverReason.HP_ZERO;
            } else if (fullText.contains("Game Over - 지식 0")) {
                reason = GameOverReason.KNOWLEDGE_ZERO;
            }

            new GameOverOverlay(window, reason).setVisible(true);
            return;
        }

        // ✅ 점심 클리어 시 → 저녁 스테이지로 전환
        if (logic.isCleared()) {
            window.dispose();
            SwingUtilities.invokeLater(onClear);
            return;
        }
    }

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
