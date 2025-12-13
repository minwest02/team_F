package game.ui.title;

import game.ui.NoonGuiController;
import game.ui.intro.IntroSequenceRunner;

import javax.swing.*;

/**
 * TitleController
 * - 타이틀 화면의 버튼 이벤트 담당
 *
 * [한줄 요약]
 * - START → Intro 1~5 재생 → 점심(Noon)으로 이동함.
 */
public class TitleController {

    private final TitleWindow window;

    public TitleController() {
        window = new TitleWindow();
        bindEvents();
    }

    private void bindEvents() {
        window.getBtnStart().addActionListener(e -> onStart());
        window.getBtnLoad().addActionListener(e -> onLoad());
        window.getBtnExit().addActionListener(e -> onExit());
    }

    public void show() {
        SwingUtilities.invokeLater(() -> window.setVisible(true));
    }

    private void onStart() {
        // 타이틀은 일단 숨김(닫지 말고)
        window.setVisible(false);

        // ✅ Intro 1~5 연속 실행 후 → Noon 시작
        IntroSequenceRunner.start(() -> SwingUtilities.invokeLater(() -> {
            new NoonGuiController();
            window.dispose(); // 타이틀 정리
        }));
    }

    private void onLoad() {
        JOptionPane.showMessageDialog(
                window,
                "LOAD 기능은 추후 구현 예정입니다.",
                "INFO",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void onExit() {
        int result = JOptionPane.showConfirmDialog(
                window,
                "정말 종료하시겠습니까?",
                "EXIT",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
