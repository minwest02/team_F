package game.ui.intro;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class IntroScene02Overlay extends JWindow {

    private static final int W = 1200;
    private static final int H = 900;

    private static final int TYPE_INTERVAL_MS = 32;
    private static final int LINE_PAUSE_MS = 550;

    private static final float FADE_STEP = 0.03f;
    private static final int FADE_TICK_MS = 40;

    private static final float ZOOM_START = 1.00f;
    private static final float ZOOM_END   = 1.03f;
    private static final int   ZOOM_TICK_MS = 40;

    private final Runnable onFinished;
    private final ScenePanel panel;

    public IntroScene02Overlay(Runnable onFinished) {
        this.onFinished = onFinished;

        setSize(W, H);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        panel = new ScenePanel();
        setContentPane(panel);

        enableSkipControls();
    }

    public void showOverlay() {
        setVisible(true);
        requestFocus();
        panel.requestFocusInWindow();
        panel.start();
    }

    private void enableSkipControls() {
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                panel.finishNow();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();
                if (k == KeyEvent.VK_SPACE || k == KeyEvent.VK_ENTER) {
                    panel.finishNow();
                }
            }
        });
    }

    private class ScenePanel extends JPanel {

        private final BufferedImage bg;

        // ✅ Intro2 대사 (원하면 여기만 수정하면 됨)
        private final String[] lines = new String[] {
                "낮이 오면, 사람들 사이를 지나고,",
                "점심이 오면, 또 시간을 삼킨다.",
                "",
                "웃는 얼굴도, 떠드는 목소리도",
                "전부 멀리서 들리는 것 같다.",
                "",
                "나는 오늘도",
                "정해진 루트를 반복한다."
        };

        private int lineIndex = 0;
        private int charIndex = 0;
        private boolean linePausing = false;

        private float fade = 0.0f;
        private boolean fadingIn = true;
        private boolean fadingOut = false;

        private float zoom = ZOOM_START;

        private Timer typingTimer;
        private Timer fadeTimer;
        private Timer zoomTimer;

        ScenePanel() {
            setFocusable(true);
            setBackground(Color.BLACK);

            // ✅ Intro1과 동일한 로더 방식
            bg = loadImageOrNull("/assets/images/intro/2_intro.png");
        }

        private BufferedImage loadImageOrNull(String path) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) {
                    System.out.println("[Intro2] 리소스 못 찾음: " + path);
                    return null;
                }
                return ImageIO.read(is);
            } catch (Exception e) {
                System.out.println("[Intro2] 이미지 로드 실패: " + path);
                e.printStackTrace();
                return null;
            }
        }

        void start() {
            startFadeIn();
            startZoom();
            startTyping();
        }

        void finishNow() {
            stopAllTimers();
            startFadeOut();
        }

        private void startTyping() {
            typingTimer = new Timer(TYPE_INTERVAL_MS, e -> {
                if (fadingOut) return;

                if (lineIndex >= lines.length) {
                    stopTypingTimer();
                    Timer t = new Timer(650, ev -> startFadeOut());
                    t.setRepeats(false);
                    t.start();
                    return;
                }

                String curLine = lines[lineIndex];

                if (curLine.isEmpty()) {
                    lineIndex++;
                    charIndex = 0;
                    linePausing = false;
                    repaint();
                    return;
                }

                if (charIndex < curLine.length()) {
                    charIndex++;
                    repaint();
                } else {
                    if (!linePausing) {
                        linePausing = true;
                        Timer pause = new Timer(LINE_PAUSE_MS, ev -> {
                            lineIndex++;
                            charIndex = 0;
                            linePausing = false;
                            repaint();
                        });
                        pause.setRepeats(false);
                        pause.start();
                    }
                }
            });
            typingTimer.start();
        }

        private void startFadeIn() {
            fadingIn = true;
            fadingOut = false;

            fadeTimer = new Timer(FADE_TICK_MS, e -> {
                if (!fadingIn) return;
                fade += FADE_STEP;
                if (fade >= 1.0f) {
                    fade = 1.0f;
                    fadingIn = false;
                }
                repaint();
            });
            fadeTimer.start();
        }

        private void startFadeOut() {
            fadingOut = true;
            fadingIn = false;

            if (fadeTimer != null) fadeTimer.stop();

            fadeTimer = new Timer(FADE_TICK_MS, e -> {
                fade -= FADE_STEP;
                if (fade <= 0.0f) {
                    fade = 0.0f;
                    fadeTimer.stop();

                    SwingUtilities.invokeLater(() -> {
                        IntroScene02Overlay.this.setVisible(false);
                        IntroScene02Overlay.this.dispose();
                        if (onFinished != null) onFinished.run();
                    });
                }
                repaint();
            });
            fadeTimer.start();
        }

        private void startZoom() {
            zoomTimer = new Timer(ZOOM_TICK_MS, e -> {
                if (fadingOut) return;

                if (zoom < ZOOM_END) {
                    zoom += 0.0009f;
                    if (zoom > ZOOM_END) zoom = ZOOM_END;
                    repaint();
                }
            });
            zoomTimer.start();
        }

        private void stopTypingTimer() {
            if (typingTimer != null) typingTimer.stop();
        }

        private void stopAllTimers() {
            if (typingTimer != null) typingTimer.stop();
            if (fadeTimer != null) fadeTimer.stop();
            if (zoomTimer != null) zoomTimer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            // 1) 배경 (zoom + fade)
            if (bg != null) {
                int drawW = Math.round(W * zoom);
                int drawH = Math.round(H * zoom);
                int x = (W - drawW) / 2;
                int y = (H - drawH) / 2;

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
                g2.drawImage(bg, x, y, drawW, drawH, null);
            }

            // 2) 텍스트 박스 (Intro1 동일)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * fade));
            int boxX = 80, boxY = 80, boxW = 760, boxH = 480;

            g2.setColor(Color.BLACK);
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 24, 24);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f * fade));
            g2.setColor(new Color(120, 255, 160));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 24, 24);

            // 3) 타이핑 텍스트 (Intro1 동일)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * fade));
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 22));
            g2.setColor(new Color(210, 255, 220));

            int tx = boxX + 32;
            int ty = boxY + 44;
            int lineGap = 32;

            for (int i = 0; i <= lineIndex && i < lines.length; i++) {
                String line = lines[i];

                if (i == lineIndex && !line.isEmpty()) {
                    int end = Math.min(charIndex, line.length());
                    line = line.substring(0, end);
                }

                if (lines[i].isEmpty()) {
                    ty += lineGap;
                    continue;
                }

                g2.drawString(line, tx, ty);
                ty += lineGap;
            }

            // 4) 스킵 안내 (Intro1 동일)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f * fade));
            g2.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
            g2.setColor(new Color(170, 255, 190));
            g2.drawString("클릭 / SPACE / ENTER 로 스킵", boxX + 40, boxY + boxH + 40);

            // 5) 페이드 보정용 검정 덮개 (Intro1 동일)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - fade));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);

            g2.dispose();
        }
    }
}
