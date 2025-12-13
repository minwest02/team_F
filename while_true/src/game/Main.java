package game;

import javax.swing.SwingUtilities;
import game.ui.title.TitleController;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TitleController().show();
        });
    }
}
