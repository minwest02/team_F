package game.ui.intro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Random;

/**
 * IntroScene05Overlay
 *
 * [역할]
 * - Intro5(루프 확정) 연출 씬
 * - JavaFX 없이 Swing만 사용
 *
 * [연출 포인트]
 * - 배경(eye+glitch) 위에 타이핑 텍스트
 * - 3초 정도에 걸쳐 노이즈/글리치 강도 상승
 * - 화면이 아주 천천히 "다가오는(zoom in)" 느낌
 *
 * [조작]
 * - 클릭/아무 키 → 즉시 종료 → onFinished 실행
 */
public class IntroScene05Overlay extends JWindow {

    private final Runnable onFinished;

    public IntroScene05Overlay(Runnable onFinished) {
        this.onFinished = onFinished;

        setSize(1200, 900);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        setContentPane(new ScenePanel());

        // 스킵
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { finish(); }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { finish(); }
        });
    }

    public void showOverlay() {
        setVisible(true);
        requestFocus();
    }

    private void finish() {
        setVisible(false);
        dispose();
        if (onFinished != null) onFinished.run();
    }

    // ===================== 내부 패널 =====================
    private class ScenePanel extends JPanel {

        private BufferedImage background;

        // Intro5는 "반복 확정"이 핵심
        private final String[] lines = {
                "…이상하다.",
                "",
                "어제와 같은 위치.",
                "어제와 같은 말.",
                "어제와 같은 숨소리.",
                "",
                "기시감이 아니라…",
                "반복이다.",
                "",
                "그리고 나는,",
                "그 사실을 이제야 알아챘다."
        };

        // 타이핑
        private int lineIndex = 0;
        private int charIndex = 0;
        private String currentLine = "";
        private Timer typingTimer;

        // FX
        private final Random rand = new Random();
        private Timer fxTimer;

        // 시간 기반 강도(0.0 ~ 1.0)
        private long startMs;

        // 줌/글리치
        private double zoom = 1.00;      // 1.00 -> 1.06 정도까지 아주 천천히
        private int shakeX = 0;
        private int shakeY = 0;

        ScenePanel() {
            try {
                background = ImageIO.read(
                        getClass().getResource("/assets/images/intro/5_intro.png")
                );
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("Intro5 이미지 로드 실패: " + e.getMessage());
            }

            setFont(new Font("Dialog", Font.PLAIN, 24));

            startMs = System.currentTimeMillis();

            // 타이핑: 약간 급박하게
            typingTimer = new Timer(36, e -> {
                if (lineIndex >= lines.length) {
                    typingTimer.stop();
                    return;
                }
                String target = lines[lineIndex];

                if (charIndex < target.length()) {
                    currentLine += target.charAt(charIndex++);
                } else {
                    currentLine = "";
                    charIndex = 0;
                    lineIndex++;
                }
                repaint();
            });
            typingTimer.start();

            // FX: 60fps에 가깝게
            fxTimer = new Timer(16, e -> {
                // 3초 동안 0->1로 상승(그 이후엔 1로 유지)
                long now = System.currentTimeMillis();
                double t = (now - startMs) / 3000.0;
                if (t > 1.0) t = 1.0;

                // 줌: 1.00 -> 1.06 (눈이 천천히 다가오는 느낌)
                zoom = 1.00 + 0.06 * t;

                // 흔들림: t가 올라갈수록 더 자주/더 크게
                if (rand.nextDouble() < 0.08 + 0.22 * t) {
                    shakeX = (rand.nextBoolean() ? 1 : -1) * (int)(1 + 6 * t);
                    shakeY = (rand.nextBoolean() ? 1 : -1) * (int)(1 + 4 * t);
                } else {
                    shakeX = 0;
                    shakeY = 0;
                }

                repaint();
            });
            fxTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // 강도 t (0~1)
            double t = (System.currentTimeMillis() - startMs) / 3000.0;
            if (t > 1.0) t = 1.0;

            // ===== 1) 배경 (줌 + 흔들림) =====
            drawZoomedBackground(g2, t);

            // ===== 2) 노이즈/글리치 (t에 따라 강해짐) =====
            drawNoise(g2, t);
            drawGlitchLines(g2, t);

            // ===== 3) 텍스트 박스 =====
            int boxX = 80;
            int boxY = 90;
            int boxW = 720;
            int boxH = 560;

            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 20, 20);

            // 테두리는 더 “형광그린” 느낌
            g2.setColor(new Color(80, 220, 140, 210));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 20, 20);

            // 텍스트 색도 약간 그린톤
            g2.setFont(getFont());
            g2.setColor(new Color(200, 255, 220));

            int x = boxX + 30;
            int y = boxY + 55;
            int gap = 32;

            for (int i = 0; i < lineIndex; i++) {
                g2.drawString(lines[i], x, y);
                y += gap;
            }

            g2.drawString(currentLine, x, y);

            // 커서
            if ((System.currentTimeMillis() / 260) % 2 == 0) {
                int cursorX = x + g2.getFontMetrics().stringWidth(currentLine) + 6;
                g2.drawString("▮", cursorX, y);
            }

            // ===== 4) 마지막: t가 1에 가까우면 화면 살짝 어둡게(흡입감) =====
            if (t > 0.75) {
                int alpha = (int)(120 * (t - 0.75) / 0.25);
                if (alpha > 120) alpha = 120;
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        }

        private void drawZoomedBackground(Graphics2D g2, double t) {
            if (background == null) {
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                return;
            }

            int w = getWidth();
            int h = getHeight();

            // 줌(중앙 기준)
            int drawW = (int)(w * zoom);
            int drawH = (int)(h * zoom);

            int x = (w - drawW) / 2 + shakeX;
            int y = (h - drawH) / 2 + shakeY;

            g2.drawImage(background, x, y, drawW, drawH, null);
        }

        /** 점 노이즈: 시간이 지날수록 밀도 증가 */
        private void drawNoise(Graphics2D g2, double t) {
            int dots = (int)(120 + 620 * t); // 120 -> 740
            g2.setColor(new Color(255, 255, 255, (int)(12 + 28 * t)));

            for (int i = 0; i < dots; i++) {
                int x = rand.nextInt(getWidth());
                int y = rand.nextInt(getHeight());
                int s = 1 + rand.nextInt(2);
                g2.fillRect(x, y, s, s);
            }
        }

        /** 수평 글리치 라인: 시간이 지날수록 자주/길게 */
        private void drawGlitchLines(Graphics2D g2, double t) {
            int lines = (int)(3 + 18 * t); // 3 -> 21
            int alpha = (int)(18 + 55 * t);

            for (int i = 0; i < lines; i++) {
                int y = rand.nextInt(getHeight());
                int w = 80 + rand.nextInt((int)(220 + 420 * t)); // 길이 증가
                int x = rand.nextInt(Math.max(1, getWidth() - w));

                // 약간 그린톤 글리치
                g2.setColor(new Color(120, 255, 180, alpha));
                g2.fillRect(x, y, w, 1);

                // 가끔 두 줄로 “찢김” 느낌
                if (rand.nextDouble() < 0.25 * t) {
                    int y2 = Math.min(getHeight() - 1, y + 2);
                    g2.setColor(new Color(255, 255, 255, alpha / 2));
                    g2.fillRect(x + rand.nextInt(15), y2, Math.max(5, w - rand.nextInt(30)), 1);
                }
            }
        }
    }
}
