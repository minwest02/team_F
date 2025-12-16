package game.ui.title;

import javax.swing.*;
import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * TitleWindow
 * - 타이틀 화면 UI 담당함
 * - 배경 이미지에 타이틀이 포함되어 있으므로
 *   코드 상 텍스트 타이틀은 출력하지 않음
 */
public class TitleWindow extends JFrame {

    private final JButton btnStart = new JButton("START");
    private final JButton btnLoad  = new JButton("LOAD");
    private final JButton btnExit  = new JButton("EXIT");

    public TitleWindow() {
        setTitle("While: True");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);
        setLocationRelativeTo(null);
        setResizable(false);

        TitlePanel panel = new TitlePanel();
        panel.setLayout(null);

        styleButton(btnStart);
        styleButton(btnLoad);
        styleButton(btnExit);

        // 버튼 위치 (필요하면 미세조정)
        int w = 260, h = 55;
        int cx = (1200 - w) / 2;
        btnStart.setBounds(cx, 520, w, h);
        btnLoad .setBounds(cx, 590, w, h);
        btnExit .setBounds(cx, 660, w, h);

        panel.add(btnStart);
        panel.add(btnLoad);
        panel.add(btnExit);

        setContentPane(panel);
    }

    private void styleButton(JButton b) {
        b.setFocusPainted(false);
        b.setFont(new Font("Dialog", Font.BOLD, 20));
        b.setBackground(new Color(20, 20, 20));
        b.setForeground(new Color(220, 255, 220));
        b.setBorder(BorderFactory.createLineBorder(new Color(120, 255, 120), 2));
    }

    public JButton getBtnStart() { return btnStart; }
    public JButton getBtnLoad()  { return btnLoad; }
    public JButton getBtnExit()  { return btnExit; }

    /**
     * TitlePanel
     * - 배경 이미지만 출력하는 패널
     */
    static class TitlePanel extends JPanel {

        private BufferedImage bgImage;
        // 만약 로고도 그리는 구조면 이것도 추가
        // private BufferedImage logoImage;

        public TitlePanel() {
            // ✅ classpath 기준 로드로 변경
            String bgPath = "/assets/images/title/title_bg.png";

            try {
                // 1) 가장 안전한 방식: getResourceAsStream
                bgImage = ImageIO.read(getClass().getResourceAsStream(bgPath));

                // (선택) 로고도 BufferedImage로 그릴 거면 동일 방식 사용
                // String logoPath = "/assets/images/title/title_logo.png";
                // logoImage = ImageIO.read(getClass().getResourceAsStream(logoPath));

            } catch (Exception e) {
                System.out.println("[TitlePanel] 배경 이미지 로드 실패: " + bgPath);
                e.printStackTrace();
                bgImage = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            if (bgImage != null) {
                g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
            } else {
                // fallback
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            // (선택) 로고 직접 그리는 경우
            // if (logoImage != null) {
            //     int x = (getWidth() - logoImage.getWidth()) / 2;
            //     int y = 120;
            //     g2.drawImage(logoImage, x, y, null);
            // }

            g2.dispose();
        }
    }
}