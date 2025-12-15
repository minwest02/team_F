package game.ui.intro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Random;
import java.io.InputStream;

/**
 * IntroScene04Overlay
 *
 * [역할]
 * - Intro4(과제를 "미루기" 직전) 분위기 연출 씬
 * - JavaFX 없이 Swing만 사용
 *
 * [연출]
 * - 배경 이미지 위에 텍스트 박스 + 타이핑 애니메이션
 * - 약한 글리치(좌우 흔들림) + 노이즈(점/라인) 오버레이
 * - ✅ 타이핑 종료 후: 화면 검정 덮기 + 중앙 문구 "이 다음날부터였어..." 쾅! 등장
 *
 * [조작]
 * - 마우스 클릭/키 입력 → 즉시 종료 → onFinished 실행
 */
public class IntroScene04Overlay extends JWindow {

    private final Runnable onFinished;

    public IntroScene04Overlay(Runnable onFinished) {
        this.onFinished = onFinished;

        setSize(1200, 900);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        setContentPane(new ScenePanel());

        // 클릭/키로 스킵
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { finish(); }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { finish(); }
        });
    }

    /** 외부에서 호출: 인트로 씬 표시 */
    public void showOverlay() {
        setVisible(true);
        requestFocus();
    }

    /** 종료 + 다음 씬으로 연결 */
    private void finish() {
        setVisible(false);
        dispose();
        if (onFinished != null) onFinished.run();
    }

    // ===================== 내부 패널 =====================
    private class ScenePanel extends JPanel {

        private BufferedImage background;

        // (C안 흐름 기준) "미루기" 직전의 위험한 자기합리화
        private final String[] lines = {
                "손가락이 멈췄다.",
                "",
                "한 줄만 더… 하면 되는데,",
                "왜 오늘은 그게 안 되지.",
                "",
                "“내일 하면 되잖아.”",
                "",
                "그 말이,",
                "가장 위험하다는 것도 모르고."
        };

        // 타이핑 상태
        private int lineIndex = 0;
        private int charIndex = 0;
        private String currentLine = "";
        private Timer typingTimer;

        // 글리치/노이즈
        private final Random rand = new Random();
        private int glitchX = 0;             // 순간 흔들림 오프셋
        private int glitchTicks = 0;         // 흔들림 지속 프레임
        private Timer fxTimer;               // 효과용 타이머

        // ===== 전환(Interlude) 연출 상태 =====
        private boolean interlude = false;          // 전환 연출 중인지
        private float interludeAlpha = 0.0f;        // 문구 투명도(0~1)
        private float interludeScale = 2.4f;        // 시작 크게 -> 1.0으로 수축
        private int   interludeShake = 0;           // 순간 흔들림
        private int   interludeFlash = 0;           // 플래시 라인 프레임
        private Timer interludeTimer;
        private boolean interludeStarted = false;

        ScenePanel() {
            // 이미지 로드 (classpath)
            background = loadImageOrNull("/assets/images/intro/4_intro.png");

            // 폰트/색감(청록+흰 느낌)
            setFont(new Font("Dialog", Font.PLAIN, 26));

            // 타이핑 속도(조금 숨 막히게)
            typingTimer = new Timer(42, e -> {
                // 전환 연출 중이면 타이핑/갱신 금지
                if (interlude) return;

                if (lineIndex >= lines.length) {
                    typingTimer.stop();

                    // ✅ 타이핑 종료 후 잠깐 텀 -> 전환 연출 시작
                    Timer t = new Timer(450, ev -> startInterlude());
                    t.setRepeats(false);
                    t.start();
                    return;
                }

                String target = lines[lineIndex];

                // 빈 줄 처리: 바로 다음 줄로
                if (target.isEmpty()) {
                    currentLine = "";
                    charIndex = 0;
                    lineIndex++;
                    repaint();
                    return;
                }

                if (charIndex < target.length()) {
                    currentLine += target.charAt(charIndex++);
                } else {
                    // 다음 줄로
                    currentLine = "";
                    charIndex = 0;
                    lineIndex++;
                }
                repaint();
            });
            typingTimer.start();

            // 효과 타이머: 노이즈/글리치 갱신
            fxTimer = new Timer(60, e -> {
                if (interlude) {
                    // 전환 중엔 배경 글리치 대신 "문구 흔들림"만 살짝
                    if (rand.nextDouble() < 0.25) interludeShake = (rand.nextBoolean() ? 1 : -1) * (1 + rand.nextInt(3));
                    else interludeShake = 0;
                    repaint();
                    return;
                }

                // 가끔(약 12%) 글리치 발동
                if (glitchTicks <= 0 && rand.nextDouble() < 0.12) {
                    glitchTicks = 2 + rand.nextInt(3);      // 2~4프레임
                    glitchX = (rand.nextBoolean() ? 1 : -1) * (3 + rand.nextInt(8)); // 3~10px
                }

                if (glitchTicks > 0) {
                    glitchTicks--;
                    if (glitchTicks == 0) glitchX = 0;
                }

                repaint();
            });
            fxTimer.start();
        }

        private BufferedImage loadImageOrNull(String path) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) {
                    System.err.println("Intro4 리소스 못 찾음: " + path);
                    return null;
                }
                return ImageIO.read(is);
            } catch (IOException e) {
                System.err.println("Intro4 이미지 로드 실패: " + e.getMessage());
                return null;
            }
        }

        private void stopTimersForInterlude() {
            if (typingTimer != null) typingTimer.stop();
            // fxTimer는 유지(전환 중 흔들림 재료로 사용)
        }

        private void startInterlude() {
            if (interludeStarted) return;
            interludeStarted = true;

            stopTimersForInterlude();

            interlude = true;
            interludeAlpha = 0.0f;
            interludeScale = 2.4f;
            interludeFlash = 6; // 처음 몇 프레임은 라인 플래시

            // "쾅!" 애니메이션: 빠르게 나타나고 수축
            interludeTimer = new Timer(16, e -> {
                // alpha: 빠르게 1.0으로
                interludeAlpha += 0.05f;
                if (interludeAlpha > 0.05f) interludeAlpha = 1.0f;

                // scale: 큰 글씨 -> 1.0으로 수축
                interludeScale -= 0.18f;
                if (interludeScale < 1.0f) interludeScale = 1.0f;

                if (interludeFlash > 0) interludeFlash--;

                repaint();

                // 수축 완료 + alpha 완료면 잠깐 유지 후 종료
                if (interludeAlpha >= 1.0f && interludeScale <= 1.0f) {
                    interludeTimer.stop();

                    Timer hold = new Timer(850, ev -> {
                        // 전환 연출 끝나면 다음 씬으로
                        finish();
                    });
                    hold.setRepeats(false);
                    hold.start();
                }
            });
            interludeTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            // ===================== 전환 연출 모드 =====================
            if (interlude) {
                drawInterlude(g2);
                g2.dispose();
                return;
            }

            // ===================== 기본 Intro4 모드 =====================

            // 배경 이미지 (글리치 시 살짝 흔들림)
            if (background != null) {
                g2.drawImage(background, glitchX, 0, getWidth(), getHeight(), null);
            } else {
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            // 노이즈 오버레이
            drawNoise(g2);

            // 텍스트 박스
            int boxX = 80;
            int boxY = 90;
            int boxW = 720;
            int boxH = 520;

            g2.setColor(new Color(0, 0, 0, 155));
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 20, 20);

            g2.setColor(new Color(120, 200, 200, 210));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 20, 20);

            // 텍스트 출력(타이핑)
            g2.setFont(getFont());
            g2.setColor(new Color(200, 235, 235));

            int x = boxX + 30;
            int y = boxY + 55;
            int lineGap = 34;

            // 이미 완성된 줄들
            for (int i = 0; i < lineIndex; i++) {
                // 빈 줄이면 간격만
                if (lines[i].isEmpty()) {
                    y += lineGap;
                    continue;
                }
                g2.drawString(lines[i], x, y);
                y += lineGap;
            }

            // 진행 중인 줄
            g2.drawString(currentLine, x, y);

            // 커서(깜빡임)
            if ((System.currentTimeMillis() / 300) % 2 == 0) {
                int cursorX = x + g2.getFontMetrics().stringWidth(currentLine) + 6;
                g2.drawString("▮", cursorX, y);
            }

            g2.dispose();
        }

        // ===================== 전환 연출 그리기 =====================
        private void drawInterlude(Graphics2D g2) {
            int w = getWidth();
            int h = getHeight();

            // 1) 전체 검정 덮기
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);

            // 2) 아주 얇은 노이즈/라인 (전환 느낌)
            drawInterludeNoise(g2);

            // 3) 중앙 문구
            String msg = "이 다음날부터였어...";

            int baseSize = 70;
            int fontSize = Math.max(12, Math.round(baseSize * interludeScale));

            Font font = new Font("맑은 고딕", Font.BOLD, fontSize);
            g2.setFont(font);

            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(msg);

            int x = (w - textW) / 2 + interludeShake;
            int y = (h / 2) + interludeShake;

            // (옵션) 처음 "쾅" 순간 플래시 라인
            if (interludeFlash > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
                g2.setColor(new Color(120, 255, 160));
                int ly = h / 2 + (rand.nextInt(41) - 20);
                g2.fillRect(0, ly, w, 2);
            }

            // 외곽선(네온)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f * interludeAlpha));
            g2.setColor(new Color(120, 255, 160));
            g2.drawString(msg, x + 2, y + 2);

            // 본문
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * interludeAlpha));
            g2.setColor(new Color(230, 255, 235));
            g2.drawString(msg, x, y);
        }

        private void drawInterludeNoise(Graphics2D g2) {
            // 아주 약한 점 노이즈
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
            g2.setColor(new Color(255, 255, 255));
            for (int i = 0; i < 180; i++) {
                int x = rand.nextInt(Math.max(1, getWidth()));
                int y = rand.nextInt(Math.max(1, getHeight()));
                g2.fillRect(x, y, 1, 1);
            }

            // 얇은 가로 라인 몇 개
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
            g2.setColor(new Color(120, 255, 160));
            for (int i = 0; i < 6; i++) {
                int y = rand.nextInt(Math.max(1, getHeight()));
                int lineW = 80 + rand.nextInt(260);
                int x = rand.nextInt(Math.max(1, getWidth() - lineW));
                g2.fillRect(x, y, lineW, 1);
            }
        }

        /** 노이즈 점/라인을 살짝만 얹어줌 */
        private void drawNoise(Graphics2D g2) {
            // 반투명 흰 점
            g2.setColor(new Color(255, 255, 255, 18));
            for (int i = 0; i < 220; i++) {
                int x = rand.nextInt(getWidth());
                int y = rand.nextInt(getHeight());
                int s = 1 + rand.nextInt(2);
                g2.fillRect(x, y, s, s);
            }

            // 아주 얇은 가로 라인
            g2.setColor(new Color(120, 200, 200, 18));
            for (int i = 0; i < 8; i++) {
                int y = rand.nextInt(getHeight());
                int w = 60 + rand.nextInt(220);
                int x = rand.nextInt(Math.max(1, getWidth() - w));
                g2.fillRect(x, y, w, 1);
            }
        }
    }
}
