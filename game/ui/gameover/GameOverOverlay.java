package game.ui.gameover;

import game.core.GameOverReason;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.net.URL;
import java.util.Random;

/**
 * GameOverOverlay (v2)
 * - 스탯별 reason에 따라 텍스트/이펙트 강도/배경 이미지가 달라지는 GameOver 오버레이
 * - 효과: 페이드 + CRT 스캔라인 + 비네팅 + 화면 찢김(tear) + (선택) 그린 채널 분리/고스트 + 줌 펄스 + 텍스트 글리치
 * - 닫기: 클릭 / ESC / ENTER / SPACE
 */
public class GameOverOverlay extends JDialog {

    // ===================== 리소스 후보(프로젝트 구조가 달라도 최대한 찾아서 로드) =====================
    // ✅ 너희가 지금 쓰는 기본 이미지(이미 존재): /assets/images/gameover/gameover_bg.png
    // ✅ 추가로 엔딩별로 배경을 다르게 쓰고 싶으면 아래 이름으로 파일만 넣으면 자동 적용됨:
    //   HP:        /assets/images/gameover/go_hp.png
    //   MENTAL:    /assets/images/gameover/go_mental.png
    //   KNOWLEDGE: /assets/images/gameover/go_knowledge.png
    //   SOCIAL:    /assets/images/gameover/go_social.png
    //
    // 노이즈도 엔딩별로 따로 쓰고 싶으면:
    //   /assets/images/gameover/noise_hp.png ... 이런 식으로 추가하면 됨.

    private static final String[] BG_DEFAULT = {
            "/assets/images/gameover/gameover_bg.png",
            "/assets/images/gameover/bg.png",
            "/assets/images/gameover/background.png"
    };

    private static final String[] NOISE_DEFAULT = {
            "/assets/images/gameover/gameover_noise.png",
            "/assets/images/gameover/noise.png",
            "/assets/images/gameover/crt_noise.png"
    };

    // 엔딩별 배경 후보
    private static final String[] BG_HP = {
            "/assets/images/gameover/go_hp.png",
            "/assets/images/gameover/hp.png",
            "/assets/images/gameover/gameover_hp.png",
    };
    private static final String[] BG_MENTAL = {
            "/assets/images/gameover/go_mental.png",
            "/assets/images/gameover/mental.png",
            "/assets/images/gameover/gameover_mental.png",
            "/assets/images/gameover/gameover_bg.png", // fallback
    };
    private static final String[] BG_KNOWLEDGE = {
            "/assets/images/gameover/go_knowledge.png",
            "/assets/images/gameover/knowledge.png",
            "/assets/images/gameover/gameover_knowledge.png",
    };
    private static final String[] BG_SOCIAL = {
            "/assets/images/gameover/go_social.png",
            "/assets/images/gameover/social.png",
            "/assets/images/gameover/gameover_social.png",
    };

    // 엔딩별 노이즈 후보(있으면 사용, 없으면 기본 노이즈/폴백)
    private static final String[] NOISE_HP = {
            "/assets/images/gameover/noise_hp.png"
    };
    private static final String[] NOISE_MENTAL = {
            "/assets/images/gameover/noise_mental.png",
            "/assets/images/gameover/gameover_noise.png"
    };
    private static final String[] NOISE_KNOWLEDGE = {
            "/assets/images/gameover/noise_knowledge.png"
    };
    private static final String[] NOISE_SOCIAL = {
            "/assets/images/gameover/noise_social.png"
    };

    // ===================== 상태 =====================
    private final JFrame owner;
    private final GameOverReason reason;
    private final Random rnd = new Random();

    private BufferedImage bg;        // 원본 배경
    private BufferedImage bgGreen;   // 그린 틴트(고스트용)
    private BufferedImage noise;     // 노이즈 이미지(있으면)

    private BufferedImage frameCache; // 찢김/줌 포함해서 한 프레임에 쓸 합성 캐시(성능/tear용)

    // 애니메이션 파라미터
    private float fade = 0f;         // 0~1
    private float noiseAlpha = 0f;   // 0~1
    private float vignette = 0f;     // 0~1
    private float scanline = 0f;     // 0~1
    private float tear = 0f;         // 0~1
    private float ghost = 0f;        // 0~1 (그린 분리/잔상)
    private float pulse = 0f;        // 0~1 (줌 펄스)
    private float textGlitch = 0f;   // 0~1

    private int shakeX = 0;
    private int shakeY = 0;

    private long tick = 0;

    private Timer timer;
    
 // 시간 기반 효과 감쇠용
    private float timeFade = 1.0f;   // 1.0 → 0.0
    private static final float FADE_OUT_SPEED = 0.0045f; // 줄어드는 속도
    
    private static final long EFFECT_DURATION_MS = 3000; // 3초
    private long startTime;
    private boolean cutDone = false;


    // ===================== 생성자 =====================
    public GameOverOverlay(JFrame owner) {
        this(owner, GameOverReason.UNKNOWN);
    }

    public GameOverOverlay(JFrame owner, GameOverReason reason) {
        super(owner, true);
        this.owner = owner;
        this.reason = (reason == null) ? GameOverReason.UNKNOWN : reason;

        setUndecorated(true);

        int w = (owner != null && owner.getWidth() > 0) ? owner.getWidth() : 1200;
        int h = (owner != null && owner.getHeight() > 0) ? owner.getHeight() : 900;

        setSize(w, h);
        setLocationRelativeTo(owner);

        // 리소스 로드(엔딩별 우선)
        bg = loadReasonBg(this.reason, w, h);
        noise = loadReasonNoise(this.reason, w, h);

        // 그린 틴트(멘탈/시스템쪽에서 쓰면 좋음)
        if (bg != null) {
            bgGreen = tint(bg, 0.55f, 1.00f, 0.55f); // R,G,B 스케일
        }

        setContentPane(new OverlayPanel());
        setBackground(new Color(0, 0, 0, 0));

        // 닫기
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();
                if (k == KeyEvent.VK_ESCAPE || k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) {
                    close();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { close(); }
        });

        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                setAlwaysOnTop(true);
                toFront();
                requestFocusInWindow();
                setAlwaysOnTop(false);
            }
        });

        startAnim();
    }

    private void close() {
        if (timer != null) timer.stop();
        dispose();
    }

    // ===================== 애니메이션 강도(엔딩별) =====================
    private void startAnim() {
        // 엔딩별 “체감 차이” 확 나게 세팅함
        final float targetNoise;
        final float targetVignette;
        final float targetScan;
        final float targetTear;
        final float targetGhost;
        final float targetPulse;
        final float targetTextGlitch;
        final int shakePower;

        switch (reason) {
            case MENTAL_ZERO -> {
                targetNoise = 0.55f;
                targetVignette = 0.70f;
                targetScan = 0.70f;
                targetTear = 0.85f;
                targetGhost = 0.80f;
                targetPulse = 0.55f;
                targetTextGlitch = 0.80f;
                shakePower = 6;
            }
            case HP_ZERO -> {
                targetNoise = 0.18f;
                targetVignette = 0.45f;
                targetScan = 0.35f;
                targetTear = 0.20f;
                targetGhost = 0.10f;
                targetPulse = 0.65f; // 심박 느낌(줌 펄스)
                targetTextGlitch = 0.10f;
                shakePower = 2;
            }
            case KNOWLEDGE_ZERO -> {
                targetNoise = 0.28f;
                targetVignette = 0.55f;
                targetScan = 0.60f;
                targetTear = 0.35f;
                targetGhost = 0.35f;
                targetPulse = 0.25f;
                targetTextGlitch = 0.55f; // 에러로그 느낌
                shakePower = 3;
            }
            case SOCIAL_MIN -> {
                targetNoise = 0.10f;
                targetVignette = 0.80f; // 고립: 가장자리 어둡게
                targetScan = 0.25f;
                targetTear = 0.10f;
                targetGhost = 0.05f;
                targetPulse = 0.10f;
                targetTextGlitch = 0.10f;
                shakePower = 1;
            }
            default -> {
                targetNoise = 0.35f;
                targetVignette = 0.60f;
                targetScan = 0.55f;
                targetTear = 0.55f;
                targetGhost = 0.55f;
                targetPulse = 0.35f;
                targetTextGlitch = 0.55f;
                shakePower = 4;
            }
        }
        
        startTime = System.currentTimeMillis();

        timer = new Timer(33, e -> {
            tick++;

            long elapsed = System.currentTimeMillis() - startTime;

            // 항상 페이드 인은 진행
            fade = Math.min(1.0f, fade + 0.04f);

            // 🔥 3초 전: 정신없는 상태
            if (elapsed < EFFECT_DURATION_MS) {

                noiseAlpha = targetNoise;
                vignette   = targetVignette;
                scanline   = targetScan;
                tear       = targetTear;
                ghost      = targetGhost;
                pulse      = targetPulse;
                textGlitch = targetTextGlitch;

                shakeX = rnd.nextInt(shakePower * 2 + 1) - shakePower;
                shakeY = rnd.nextInt(shakePower * 2 + 1) - shakePower;

            }
            // ❗ 3초 이후: 툭! 하고 정적
            else {

                if (!cutDone) {
                    // 한 번만 실행되는 컷 연출
                    noiseAlpha = 0f;
                    vignette   = 0f;
                    scanline   = 0f;
                    tear       = 0f;
                    ghost      = 0f;
                    pulse      = 0f;
                    textGlitch = 0f;
                    shakeX = 0;
                    shakeY = 0;

                    cutDone = true;
                }
            }

            repaint();
        });
        timer.start();

    }

    private float approach(float cur, float target, float step) {
        if (cur < target) return Math.min(target, cur + step);
        if (cur > target) return Math.max(target, cur - step);
        return cur;
    }

    // ===================== 텍스트 =====================
    private String endingTitle() {
        return "GAME OVER";
    }

    private String endingSubBase() {
        return switch (reason) {
            case HP_ZERO -> "기절 엔딩 (HP 0)";
            case MENTAL_ZERO -> "침잠 엔딩 (MENTAL 0)";
            case KNOWLEDGE_ZERO -> "학사경고 엔딩 (KNOWLEDGE 0)";
            case SOCIAL_MIN -> "고립 엔딩 (SOCIAL MIN)";
            default -> "SYSTEM FAILED";
        };
    }

    private String endingHint() {
        return "Click / ESC / ENTER";
    }

    // 텍스트 글리치(한두 프레임만 깨졌다가 돌아오는 느낌)
    private String glitchify(String s) {
        if (s == null || s.isEmpty()) return s;
        // 확률적으로만 깨기
        float chance = 0.08f + 0.20f * textGlitch;
        if (rnd.nextFloat() > chance) return s;

        char[] a = s.toCharArray();
        int n = 1 + rnd.nextInt(Math.max(1, a.length / 6));
        for (int i = 0; i < n; i++) {
            int idx = rnd.nextInt(a.length);
            char c = a[idx];
            // 숫자/알파/공백은 유지하고, 일부만 교체
            if (c != ' ') a[idx] = (rnd.nextBoolean() ? '#' : (rnd.nextBoolean() ? '!' : '%'));
        }
        return new String(a);
    }

    // ===================== 패널 =====================
    private class OverlayPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int W = getWidth();
            int H = getHeight();

            // 프레임 캐시(tear/줌 처리용)
            if (frameCache == null || frameCache.getWidth() != W || frameCache.getHeight() != H) {
                frameCache = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            }

            // 1) 프레임 합성(배경/고스트/노이즈/스캔라인/비네팅)
            Graphics2D fg = frameCache.createGraphics();
            fg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // base black
            fg.setComposite(AlphaComposite.SrcOver);
            fg.setColor(Color.BLACK);
            fg.fillRect(0, 0, W, H);

            // 줌 펄스(심박/불안)
            float zoom = 1.0f + (float)Math.sin(tick * 0.10) * (0.03f + 0.05f * pulse);
            int zw = (int)(W * zoom);
            int zh = (int)(H * zoom);
            int zx = (W - zw) / 2;
            int zy = (H - zh) / 2;

            // 배경 이미지
            if (bg != null) {
                fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.60f * clamp01(fade)));
                fg.drawImage(bg, zx + shakeX, zy + shakeY, zw, zh, null);

                // 그린 고스트(채널 분리 느낌)
                if (bgGreen != null && ghost > 0.01f) {
                    int ox = shakeX + (int)(Math.sin(tick * 0.35) * (6 + 18 * ghost));
                    int oy = shakeY + (int)(Math.cos(tick * 0.27) * (3 + 10 * ghost));
                    fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f * ghost));
                    fg.drawImage(bgGreen, zx + ox, zy + oy, zw, zh, null);
                }
            }

            // 노이즈 이미지(있으면)
            if (noise != null) {
                float a = clamp01(noiseAlpha) * (0.65f + 0.35f * (float)Math.sin(tick * 0.18));
                fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
                fg.drawImage(noise, 0, 0, W, H, null);
            } else {
                // 폴백 노이즈(점)
                fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f + 0.10f * noiseAlpha));
                fg.setColor(Color.WHITE);
                int dots = 1500 + (int)(2500 * noiseAlpha);
                for (int i = 0; i < dots; i++) {
                    int x = rnd.nextInt(W);
                    int y = rnd.nextInt(H);
                    fg.drawRect(x, y, 1, 1);
                }
            }

            // CRT 스캔라인
            if (scanline > 0.01f) {
                fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f * scanline));
                fg.setColor(Color.BLACK);
                int step = Math.max(2, 5 - (int)(scanline * 3)); // scanline 강할수록 촘촘
                for (int y = 0; y < H; y += step) {
                    fg.drawLine(0, y, W, y);
                }
            }

            // 비네팅(가장자리 어둡게)
            if (vignette > 0.01f) {
                Paint old = fg.getPaint();
                fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * vignette));
                RadialGradientPaint rgp = new RadialGradientPaint(
                        new Point(W / 2, H / 2),
                        (float)Math.max(W, H) * 0.65f,
                        new float[]{0.0f, 0.65f, 1.0f},
                        new Color[]{
                                new Color(0, 0, 0, 0),
                                new Color(0, 0, 0, 60),
                                new Color(0, 0, 0, 220)
                        }
                );
                fg.setPaint(rgp);
                fg.fillRect(0, 0, W, H);
                fg.setPaint(old);
            }

            // 페이드(전체 어둡게 + 등장)
            fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.90f * clamp01(fade)));
            fg.setColor(new Color(0, 0, 0, 40));
            fg.fillRect(0, 0, W, H);

            fg.dispose();

            // 2) 찢김(tear) 적용해서 실제 화면에 뿌리기
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp01(fade)));

            if (tear <= 0.01f) {
                g2.drawImage(frameCache, 0, 0, null);
            } else {
                // 화면을 여러 띠로 나눠서 x오프셋을 다르게(tear)
                int bands = 6 + (int)(tear * 18);
                int bandH = Math.max(6, H / bands);

                for (int y = 0; y < H; y += bandH) {
                    int h = Math.min(bandH, H - y);

                    // 띠별 오프셋: 멘탈에서 더 크게
                    int maxOff = (int)(6 + 40 * tear);
                    int off = (int)(Math.sin((tick * 0.25) + y * 0.05) * maxOff);
                    // 확률적으로 “툭” 튀는 찢김
                    if (rnd.nextFloat() < 0.08f * tear) {
                        off += rnd.nextInt(maxOff * 2 + 1) - maxOff;
                    }

                    g2.drawImage(frameCache,
                            off, y, off + W, y + h,
                            0, y, W, y + h,
                            null);
                }
            }

            // 3) 텍스트(글리치 포함)
            drawCenteredText(g2, W, H);

            g2.dispose();
        }

        private void drawCenteredText(Graphics2D g2, int W, int H) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            String t1 = endingTitle();
            String t2 = glitchify(endingSubBase());
            String t3 = endingHint();

            int cx = W / 2;
            int cy = H / 2;

            // 타이틀(약간 흔들/글리치)
            int tx = cx + (int)(Math.sin(tick * 0.40) * (2 + 6 * textGlitch));
            int ty = cy - 60 + (int)(Math.cos(tick * 0.33) * (1 + 4 * textGlitch));

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp01(fade)));
            g2.setFont(new Font("Dialog", Font.BOLD, Math.max(58, W / 16)));
            drawStringCenterGlow(g2, t1, tx, ty, 2 + (int)(3 * textGlitch));

            // 서브
            g2.setFont(new Font("Dialog", Font.PLAIN, Math.max(22, W / 45)));
            drawStringCenterGlow(g2, t2, cx, cy + 10, 1);

            // 힌트(하단)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
            g2.setFont(new Font("Dialog", Font.PLAIN, Math.max(16, W / 70)));
            drawStringCenter(g2, t3, cx, H - 55, new Color(200, 255, 200));
        }

        private void drawStringCenterGlow(Graphics2D g2, String s, int cx, int y, int glow) {
            FontMetrics fm = g2.getFontMetrics();
            int x = cx - fm.stringWidth(s) / 2;

            // glow(검정 그림자 여러번)
            g2.setColor(new Color(0, 0, 0, 180));
            for (int i = 1; i <= glow; i++) {
                g2.drawString(s, x + i, y + i);
                g2.drawString(s, x - i, y + i);
            }

            // 본문(형광 그린 톤)
            g2.setColor(new Color(200, 255, 200));
            g2.drawString(s, x, y);

            // 멘탈/시스템일 때 약간 더 “형광” 튀게
            if (reason == GameOverReason.MENTAL_ZERO || reason == GameOverReason.UNKNOWN) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f * clamp01(textGlitch)));
                g2.setColor(new Color(120, 255, 160));
                g2.drawString(s, x + 2, y);
                g2.drawString(s, x - 2, y);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp01(fade)));
            }
        }

        private void drawStringCenter(Graphics2D g2, String s, int cx, int y, Color c) {
            FontMetrics fm = g2.getFontMetrics();
            int x = cx - fm.stringWidth(s) / 2;

            g2.setColor(new Color(0, 0, 0, 180));
            g2.drawString(s, x + 1, y + 1);

            g2.setColor(c);
            g2.drawString(s, x, y);
        }
    }

    // ===================== 리소스 로드 =====================
    private BufferedImage loadReasonBg(GameOverReason r, int w, int h) {
        BufferedImage img = null;

        switch (r) {
            case HP_ZERO -> img = loadFirstImage(BG_HP, w, h);
            case MENTAL_ZERO -> img = loadFirstImage(BG_MENTAL, w, h);
            case KNOWLEDGE_ZERO -> img = loadFirstImage(BG_KNOWLEDGE, w, h);
            case SOCIAL_MIN -> img = loadFirstImage(BG_SOCIAL, w, h);
            default -> img = null;
        }

        if (img == null) img = loadFirstImage(BG_DEFAULT, w, h);
        return img;
    }

    private BufferedImage loadReasonNoise(GameOverReason r, int w, int h) {
        BufferedImage img = null;

        switch (r) {
            case HP_ZERO -> img = loadFirstImage(NOISE_HP, w, h);
            case MENTAL_ZERO -> img = loadFirstImage(NOISE_MENTAL, w, h);
            case KNOWLEDGE_ZERO -> img = loadFirstImage(NOISE_KNOWLEDGE, w, h);
            case SOCIAL_MIN -> img = loadFirstImage(NOISE_SOCIAL, w, h);
            default -> img = null;
        }

        if (img == null) img = loadFirstImage(NOISE_DEFAULT, w, h);
        return img;
    }

    private BufferedImage loadFirstImage(String[] candidates, int w, int h) {
        for (String p : candidates) {
            if (p == null || p.isBlank()) continue;
            URL url = getClass().getResource(p);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image image = icon.getImage();
                return toBufferedScaled(image, w, h);
            }
        }
        return null;
    }

    // ===================== 이미지 유틸 =====================
    private BufferedImage toBufferedScaled(Image src, int w, int h) {
        if (src == null) return null;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return out;
    }

    // 간단 틴트(채널 스케일)
    private BufferedImage tint(BufferedImage src, float r, float g, float b) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        float[] scales = new float[]{r, g, b, 1.0f};
        float[] offsets = new float[]{0, 0, 0, 0};
        RescaleOp op = new RescaleOp(scales, offsets, null);
        op.filter(out, out);
        return out;
    }

    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}
