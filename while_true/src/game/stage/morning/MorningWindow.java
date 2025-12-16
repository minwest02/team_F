package game.stage.morning;

import javax.swing.*;

public class MorningWindow extends JFrame {

    public MorningWindow(Runnable onClear) {
        super("Morning Stage");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // ⚠️ GamePanel이 onClear를 받도록 2번에서 수정할 예정
        GamePanel panel = new GamePanel(() -> {
            dispose();
            if (onClear != null) onClear.run();
        });

        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
}
