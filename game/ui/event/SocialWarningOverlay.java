package game.ui.event;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

/**
 * SocialWarningOverlay
 * - 사교 스탯 -5 이하 도달 시 1회성 경고 이벤트
 * - 게임은 계속 진행됨
 */
public class SocialWarningOverlay extends JDialog {

    private static final String IMAGE_PATH =
            "/assets/images/event/social_warning.png"; // 너가 넣을 이미지

    private Image image;

    public SocialWarningOverlay(JFrame owner) {
        super(owner, true);
        setUndecorated(true);

        int w = 700;
        int h = 400;
        setSize(w, h);
        setLocationRelativeTo(owner);

        image = loadImage();

        setContentPane(new Panel());

        // 닫기 입력
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER ||
                    e.getKeyCode() == KeyEvent.VK_ESCAPE ||
                    e.getKeyCode() == KeyEvent.VK_SPACE) {
                    dispose();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dispose();
            }
        });
    }

    private Image loadImage() {
        URL url = getClass().getResource(IMAGE_PATH);
        if (url != null) {
            return new ImageIcon(url).getImage();
        }
        return null;
    }

    private class Panel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0, 0, 0, 220));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // 이미지
            if (image != null) {
                int iw = 200;
                int ih = 200;
                int ix = (getWidth() - iw) / 2;
                int iy = 40;
                g2.drawImage(image, ix, iy, iw, ih, null);
            }

            // 텍스트
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setFont(new Font("Dialog", Font.BOLD, 20));
            g2.setColor(new Color(200, 255, 200));

            String msg = "당신은 학교의 소문난 히키코모리가 되었습니다";
            int tw = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (getWidth() - tw) / 2, getHeight() - 80);

            g2.setFont(new Font("Dialog", Font.PLAIN, 14));
            String hint = "(ENTER / 클릭)";
            int hw = g2.getFontMetrics().stringWidth(hint);
            g2.drawString(hint, (getWidth() - hw) / 2, getHeight() - 50);
        }
    }
}
