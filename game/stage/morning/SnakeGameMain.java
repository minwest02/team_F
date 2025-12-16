package game.stage.morning;

import javax.swing.JFrame;

public class SnakeGameMain {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Project");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        GamePanel panel = new GamePanel();
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        panel.requestFocusInWindow();
    }
}
