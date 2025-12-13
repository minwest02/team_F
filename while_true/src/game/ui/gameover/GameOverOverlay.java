package game.ui.gameover;

import game.ui.title.TitleController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

/**
 * GameOverOverlay
 *
 * [역할]
 * - 게임 오버 시 기존 게임 화면 위에 덮어씌워지는 연출 전용 오버레이
 * - 눈 소용돌이 확대 → 글리치 → 암전 → GAME OVER 출력 → 타이틀 복귀
 *
 * [특징]
 * - JFrame이 아닌 JDialog 사용 (부모 창 위에 자연스럽게 겹치기 위함)
 * - 별도의 영상 없이 이미지 + 타이머로 연출 구현
 */
public class GameOverOverlay extends JDialog {

    /* ===================== 리소스 경로 ===================== */

    // 게임오버 배경 (눈 소용돌이 이미지)
    private static final String BG_PATH =
            "/assets/images/gameover/gameover_bg.png";

    // CRT / 글리치 노이즈 오버레이 이미지
    private static final String NOISE_PATH =
            "/assets/images/gameover/gameover_noise.png";

    /* ===================== 연출 타이밍 설정 ===================== */

    // 타이머 FPS (낮을수록 거칠고, 높을수록 부드러움)
    private static final int FPS = 16;

    // 각 연출 단계별 지속 시간 (ms)
    private static final int ZOOM_TIME   = 3000; // 눈이 다가오는 시간
    private static final int GLITCH_TIME = 450;  // 글리치 폭주
    private static final int BLACK_TIME  = 200;  // 암전
    private static final int TEXT_TIME   = 900;  // GAME OVER 표시

    /* ===================== 연출 단계 정의 ===================== */

    /**
     * ZOOM   : 배경 이미지 확대 (서서히 다가옴)
     * GLITCH : 화면 흔들림 + 노이즈
     * BLACK  : 완전 암전
     * TEXT   : GAME OVER 텍스트 출력
     */
    private enum Phase { ZOOM, GLITCH, BLACK, TEXT }
    private Phase phase = Phase.ZOOM;

    // 현재 단계가 시작된 시각
    private long phaseStart = System.currentTimeMillis();

    /* ===================== 이미지 & 상태 ===================== */

    private final Image bgImg;      // 배경 이미지
    private final Image noiseImg;   // 노이즈 이미지

    // 배경 확대 비율
    private double scale = 0.98;

    // 확대 가속도 (점점 빨라지게)
    private double scaleSpeed = 0.00008;

    // 노이즈 투명도
    private float noiseAlpha = 0f;

    // 랜덤 흔들림용
    private final Random rnd = new Random();

    // 이 오버레이를 덮을 기존 게임 창
    private final JFrame owner;

    /* ===================== 생성자 ===================== */

    /**
     * @param owner 현재 게임 화면(JFrame)
     */
    public GameOverOverlay(JFrame owner) {
        super(owner, true); // modal = true → 기존 입력 차단
        this.owner = owner;

        setUndecorated(true);        // 창 테두리 제거
        setSize(1200, 900);
        setLocationRelativeTo(owner);

        // 이미지 로딩
        bgImg = new ImageIcon(BG_PATH).getImage();
        noiseImg = new ImageIcon(NOISE_PATH).getImage();

        // 커스텀 패널 설정
        setContentPane(new OverlayPanel());

        // 항상 위에 잠깐 올렸다가 해제 (포커스 문제 방지)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setAlwaysOnTop(true);
                toFront();
                requestFocus();
                setAlwaysOnTop(false);
            }
        });

        start(); // 연출 시작
    }

    /* ===================== 연출 타이머 ===================== */

    /**
     * 타이머 기반으로 단계별 연출을 진행함
     */
    private void start() {
        Timer timer = new Timer(FPS, e -> {
            long now = System.currentTimeMillis();
            long dt = now - phaseStart;

            switch (phase) {

                // 1️⃣ 눈이 서서히 다가옴
                case ZOOM -> {
                    scale += scaleSpeed * FPS * 60;
                    scaleSpeed *= 1.0005; // 점점 가속
                    noiseAlpha = 0.12f;

                    if (dt > ZOOM_TIME) {
                        phase = Phase.GLITCH;
                        phaseStart = now;
                    }
                }

                // 2️⃣ 글리치 폭주
                case GLITCH -> {
                    scale += 0.006;
                    noiseAlpha = rnd.nextBoolean()
                            ? 0.3f + rnd.nextFloat() * 0.3f
                            : 0f;

                    if (dt > GLITCH_TIME) {
                        phase = Phase.BLACK;
                        phaseStart = now;
                    }
                }

                // 3️⃣ 암전
                case BLACK -> {
                    if (dt > BLACK_TIME) {
                        phase = Phase.TEXT;
                        phaseStart = now;
                    }
                }

                // 4️⃣ GAME OVER 출력 후 종료
                case TEXT -> {
                    if (dt > TEXT_TIME) {
                        ((Timer) e.getSource()).stop();
                        finish();
                    }
                }
            }
            repaint();
        });
        timer.start();
    }

    /* ===================== 연출 종료 ===================== */

    /**
     * 게임오버 연출 종료 후
     * - 기존 게임 창 닫기
     * - 타이틀 화면으로 복귀
     */
    private void finish() {
        dispose();
        owner.dispose();
        SwingUtilities.invokeLater(() ->
                new TitleController().show()
        );
    }

    /* ===================== 실제 그리는 패널 ===================== */

    private class OverlayPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            int W = getWidth();
            int H = getHeight();

            // 암전 상태
            if (phase == Phase.BLACK) {
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, W, H);
                return;
            }

            // 텍스트 출력 상태
            if (phase == Phase.TEXT) {
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, W, H);
                drawText(g2, W, H);
                return;
            }

            // 배경 이미지 확대 계산
            int imgW = bgImg.getWidth(this);
            int imgH = bgImg.getHeight(this);

            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);

            // 글리치 시 화면 흔들림
            int shakeX = (phase == Phase.GLITCH) ? rnd.nextInt(10) - 5 : 0;
            int shakeY = (phase == Phase.GLITCH) ? rnd.nextInt(6) - 3 : 0;

            int x = (W - drawW) / 2 + shakeX;
            int y = (H - drawH) / 2 + shakeY;

            g2.drawImage(bgImg, x, y, drawW, drawH, this);

            // 노이즈 오버레이
            if (noiseAlpha > 0f) {
                g2.setComposite(
                        AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, noiseAlpha
                        )
                );
                g2.drawImage(noiseImg, 0, 0, W, H, this);
                g2.setComposite(AlphaComposite.SrcOver);
            }
        }

        /**
         * GAME OVER 텍스트 출력
         */
        private void drawText(Graphics2D g2, int W, int H) {
            g2.setFont(new Font("Dialog", Font.BOLD, 88));
            String t1 = "GAME OVER";

            int x = (W - g2.getFontMetrics().stringWidth(t1)) / 2;
            int y = H / 2 - 20;

            // 그림자
            g2.setColor(Color.BLACK);
            g2.drawString(t1, x + 4, y + 4);

            // 본문
            g2.setColor(new Color(160, 255, 160));
            g2.drawString(t1, x, y);

            // 부제
            g2.setFont(new Font("Dialog", Font.PLAIN, 36));
            String t2 = "While: True";

            int x2 = (W - g2.getFontMetrics().stringWidth(t2)) / 2;
            g2.drawString(t2, x2, y + 70);
        }
    }
}
