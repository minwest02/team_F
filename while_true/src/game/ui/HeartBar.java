package game.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class HeartBar extends JPanel {
    private final ImageIcon full;
    private final ImageIcon empty;
    private final JLabel[] hearts;

    // 아이콘 표시 크기(픽셀) — 16x16이면 32로 올려서 확실히 보이게
    private static final int ICON_SIZE = 32;

    public HeartBar(int maxHp, String fullPath, String emptyPath) {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

        full  = scaledIcon(fullPath, ICON_SIZE, ICON_SIZE);
        empty = scaledIcon(emptyPath, ICON_SIZE, ICON_SIZE);

        hearts = new JLabel[maxHp];
        for (int i = 0; i < maxHp; i++) {
            hearts[i] = new JLabel(empty);
            add(hearts[i]);
        }

        // ✅ BorderLayout/GridLayout에서 눌리지 않게 “높이 확보”
        setPreferredSize(new Dimension((ICON_SIZE + 4) * maxHp, ICON_SIZE));
        setMinimumSize(getPreferredSize());
    }

    public void setHp(int hp) {
        for (int i = 0; i < hearts.length; i++) {
            hearts[i].setIcon(i < hp ? full : empty);
        }
        revalidate();
        repaint();
    }

    private ImageIcon scaledIcon(String path, int w, int h) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(path), "리소스 못 찾음: " + path));
        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_FAST);
        return new ImageIcon(img);
    }
}
