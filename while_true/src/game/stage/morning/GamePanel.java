package game.stage.morning;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GamePanel extends JPanel implements KeyListener {

    public static final int TILE = 32;
    public static final int W = 15;
    public static final int H = 15;
    public static final int DELAY_MS = 170;

    public static final int UI_H = 160;

    private final GameState state = new GameState(W, H);
    private final Timer timer;

    // ✅ 클리어 콜백 + 중복 방지
    private final Runnable onClear;
    private boolean clearFired = false;

    // ✅ 기존 호환(기본 생성자 유지)
    public GamePanel() {
        this(null);
    }

    // ✅ 콜백 받는 생성자
    public GamePanel(Runnable onClear) {
        this.onClear = onClear;

        setPreferredSize(new Dimension(W * TILE, H * TILE + UI_H));
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(DELAY_MS, e -> {
            state.tick();

            // ✅ 클리어 감지 → 타이머 stop → 콜백 1회
            if (!clearFired && isClearedNow()) {
                clearFired = true;

                // ✅ 핵심: 람다에서 timer 필드 직접 참조 대신 "이 이벤트의 타이머"를 멈춤
                ((Timer) e.getSource()).stop();

                if (this.onClear != null) {
                    SwingUtilities.invokeLater(this.onClear);
                }
            }

            repaint();
        });
        timer.start();
    }

    // ✅ GameState의 클리어 판정 사용
    private boolean isClearedNow() {
        return state.isCleared();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Render.draw(g2, state, TILE, W, H, UI_H);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        state.onKey(e.getKeyCode());
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
