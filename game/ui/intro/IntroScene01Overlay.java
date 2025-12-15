package game.ui.intro;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * IntroScene01Overlay (Swing only)
 *
 * [역할]
 * - 타이틀에서 Start 누른 뒤, 아침 스테이지로 넘어가기 전에
 *   "세계관/주인공 소개"를 이미지 + 텍스트 타이핑으로 보여주는 인트로 씬 1.
 *
 * [특징]
 * - 배경 이미지(1_intro.png) + 반투명 텍스트 박스 + 타이핑 애니메이션
 * - 아주 미세한 줌 인
 * - 페이드 인/아웃 연출
 * - 스킵: 마우스 클릭 / 스페이스 / 엔터
 */
public class IntroScene01Overlay extends JWindow {

    // ====== 설정값 ======
    private static final int W = 1200;
    private static final int H = 900;

    private static final int TYPE_INTERVAL_MS = 32;
    private static final int LINE_PAUSE_MS = 550;

    private static final float FADE_STEP = 0.03f;
    private static final int FADE_TICK_MS = 40;

    private static final float ZOOM_START = 1.00f;
    private static final float ZOOM_END   = 1.03f;
    private static final int   ZOOM_TICK_MS = 40;

    // ====== 상태 ======
    private final Runnable onFinished;
    private final ScenePanel panel;

    public IntroScene01Overlay(Runnable onFinished) {
        this.onFinished = onFinished;

        setSize(W, H);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        panel = new ScenePanel();
        setContentPane(panel);

        enableSkipControls();
    }

    /** 외부에서 호출: 씬 시작 */
    public void showOverlay() {
        setVisible(true);
        requestFocus();
        panel.requestFocusInWindow();
        panel.start();
    }

    /** 스킵(클릭/스페이스/엔터) 처리 */
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

    // ============================================================
    // 내부 패널: 그림 + 타이핑 + 페이드 + 줌
    // ============================================================
    private class ScenePanel extends JPanel {

        // ----- 리소스 -----
        private final BufferedImage bg;

        // ----- 대사 -----
        private final String[] lines = new String[] {
                "아침이 오면 눈을 뜨고,",
                "눈을 뜨면 또 하루가 시작된다.",
                "",
                "목포대 컴퓨터공학과 2학년.",
                "해야 할 과제는 늘 그대로인데,",
                "하루는 왜 이렇게 빨리 소모되는지 모르겠다.",
                "",
                "강의실, 컴퓨터, 코드, 과제.",
                "어제와 다르지 않은 말들,",
                "어제와 다르지 않은 풍경.",
                "",
                "이게 몇 번째 하루인지,",
                "이제는 세는 것조차 의미가 없었다."
        };

        // ----- 타이핑 상태 -----
        private int lineIndex = 0;
        private int charIndex = 0;
        private boolean linePausing = false;

        // ----- 페이드/줌 상태 -----
        private float fade = 0.0f;
        private boolean fadingIn = true;
        private boolean fadingOut = false;

        private float zoom = ZOOM_START;

        // ----- 타이머 -----
        private Timer typingTimer;
        private Timer fadeTimer;
        private Timer zoomTimer;

        ScenePanel() {
            setFocusable(true);
            setBackground(Color.BLACK);

            // ✅ 배경 이미지 로드 (classpath 안정 방식)
            bg = loadImageOrNull("/assets/images/intro/1_intro.png");
        }

        private BufferedImage loadImageOrNull(String path) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) {
                    System.out.println("[Intro1] 리소스 못 찾음: " + path);
                    return null;
                }
                return ImageIO.read(is);
            } catch (Exception e) {
                System.out.println("[Intro1] 이미지 로드 실패: " + path);
                e.printStackTrace();
                return null;
            }
        }

        void start() {
            startFadeIn();
            startZoom();
            startTyping();
        }

        /** 즉시 종료(스킵) */
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
                        IntroScene01Overlay.this.setVisible(false);
                        IntroScene01Overlay.this.dispose();
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

            // ===== 1) 배경 이미지 그리기 (zoom 적용) =====
            if (bg != null) {
                int drawW = Math.round(W * zoom);
                int drawH = Math.round(H * zoom);
                int x = (W - drawW) / 2;
                int y = (H - drawH) / 2;

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
                g2.drawImage(bg, x, y, drawW, drawH, null);
            }

            // ===== 2) 텍스트 박스 =====
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * fade));
            int boxX = 80;
            int boxY = 80;
            int boxW = 760;
            int boxH = 480;

            g2.setColor(Color.BLACK);
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 24, 24);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f * fade));
            g2.setColor(new Color(120, 255, 160));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 24, 24);

            // ===== 3) 타이핑 텍스트 =====
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

            // ===== 4) 하단 스킵 안내 =====
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f * fade));
            g2.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
            g2.setColor(new Color(170, 255, 190));
            g2.drawString("클릭 / SPACE / ENTER 로 스킵", boxX + 40, boxY + boxH + 40);

            // ===== 5) 페이드 보정용 검정 덮개 =====
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - fade));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, W, H);

            g2.dispose();
        }
    }
}
