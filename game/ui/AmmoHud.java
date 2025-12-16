package game.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class AmmoHud extends JPanel {
    private final JLabel blankText = new JLabel("x0");
    private final JLabel liveText  = new JLabel("x0");

    private static final int ICON_SIZE = 32;

    public AmmoHud(String blankPath, String livePath) {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 6));

        JLabel blankImg = new JLabel(scaledIcon(blankPath, ICON_SIZE, ICON_SIZE));
        JLabel liveImg  = new JLabel(scaledIcon(livePath,  ICON_SIZE, ICON_SIZE));

        Font f = new Font("맑은 고딕", Font.BOLD, 18);
        blankText.setFont(f);
        liveText.setFont(f);

        // 색상 진하게
        blankText.setForeground(new Color(20, 20, 20));   // 거의 검정
        liveText.setForeground(new Color(20, 20, 20));

        add(blankImg); add(blankText);
        add(liveImg);  add(liveText);

        // ✅ NORTH에 붙어도 높이 확보
        setPreferredSize(new Dimension(260, ICON_SIZE + 12));
        setMinimumSize(getPreferredSize());
    }

    public void setCounts(int blank, int live) {
        blankText.setText("x" + blank);
        liveText.setText("x" + live);
        revalidate();
        repaint();
    }

    private ImageIcon scaledIcon(String path, int w, int h) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(path), "리소스 못 찾음: " + path));
        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_FAST);
        return new ImageIcon(img);
    }
}
