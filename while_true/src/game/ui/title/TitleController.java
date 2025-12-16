package game.ui.title;

import game.ui.NoonGuiController;
import game.ui.EveningGuiController;
import game.stage.night.NightWindow;
import game.ui.intro.IntroSequenceRunner;

// ✅ 아침은 GuiController가 아니라 Window로 시작함
import game.stage.morning.MorningWindow;

import javax.swing.*;

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
        window.setVisible(false);

        // ✅ Intro 1~5 → Morning(아침) → Noon(점심) → Evening(저녁) → Night(밤)
        IntroSequenceRunner.start(() -> SwingUtilities.invokeLater(() -> {

            // ✅ Morning(아침) 클리어 → Noon 시작
            new MorningWindow(() -> {

                // ✅ Noon(점심) 클리어 → Evening 시작
                new NoonGuiController(() -> {

                    // ✅ Evening(저녁) 클리어 → Night 시작
                    new EveningGuiController(() -> {
                        new NightWindow();
                    });

                });

            });

            window.dispose();
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
