package game.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameHelpOverlay_Noon extends JDialog {

    private static boolean shown = false;

    // ✅ 닫는 중 중복 호출 방지
    private boolean closing = false;

    // (선택) 부모 프레임 참조
    private final JFrame ownerFrame;

    public static void showOnce(JFrame owner) {
        if (shown) return;
        shown = true;

        GameHelpOverlay_Noon d = new GameHelpOverlay_Noon(owner);
        d.setVisible(true);
    }

    public GameHelpOverlay_Noon(JFrame owner) {
        super(owner, "도움말", true); // 모달
        this.ownerFrame = owner;

        setUndecorated(true);
        setSize(900, 520);
        setLocationRelativeTo(owner);
        setContentPane(new Panel());
        setAlwaysOnTop(true);

        // ✅ 포커스 잡기 (키 입력 안정화)
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        bindCloseControls();

        // ✅ 열리자마자 키 입력 먹게
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    // =========================
    // 닫기 처리(클릭/키)
    // =========================
    private void bindCloseControls() {
        // ✅ mousePressed에서 닫지 말고, 이벤트를 먹고 mouseReleased에서 닫기
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                e.consume(); // 아래로 이벤트 새는 것 최대한 방지
            }

            @Override public void mouseReleased(MouseEvent e) {
                e.consume();
                closeSafely();
            }
        });

        // ✅ 키도 keyReleased 기준 (눌렀다 떼는 이벤트가 더 깔끔)
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                // 눌림 이벤트는 일단 소비만
                int k = e.getKeyCode();
                if (k == KeyEvent.VK_ESCAPE || k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) {
                    e.consume();
                }
            }

            @Override public void keyReleased(KeyEvent e) {
                int k = e.getKeyCode();
                if (k == KeyEvent.VK_ESCAPE || k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) {
                    e.consume();
                    closeSafely();
                }
            }
        });
    }

    private void closeSafely() {
        if (closing) return;
        closing = true;

        // ✅ 지금 처리 중인 입력 이벤트가 끝난 다음에 닫기
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();

            // (옵션) 부모창 포커스 복구
            if (ownerFrame != null) {
                ownerFrame.toFront();
                ownerFrame.requestFocus();
            }
        });
    }

    // =========================
    // UI 패널
    // =========================
    private static class Panel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            // 배경
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));
            g2.setColor(Color.BLACK);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

            // 테두리(네온)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
            g2.setColor(new Color(120, 255, 160));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);

            // 텍스트
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setColor(new Color(210, 255, 220));

            Font title = new Font("맑은 고딕", Font.BOLD, 26);
            Font body  = new Font("맑은 고딕", Font.PLAIN, 18);
            Font hint  = new Font("맑은 고딕", Font.PLAIN, 16);

            int x = 48;
            int y = 70;

            g2.setFont(title);
            g2.drawString("점심 스테이지 안내", x, y);

            y += 44;
            g2.setFont(body);

            String[] lines = {
                    "• 아래 3가지 선택지 중 하나를 고르면 진행된다.",
                    "• 왼쪽은 기록, 가운데는 상황과 대사다.",
                    "• 누군가는 이 반복에 대한 힌트를 흘린다.",
                    "",
                    "• ESC : “정말 넘길까?” (잠깐 멈추고 싶을 때)",
                    "",
                    "닫기: 클릭 / ENTER / SPACE / ESC"
            };

            for (String s : lines) {
                g2.drawString(s, x, y);
                y += 28;
            }

            // 마지막 문구(불친절 감성)
            y += 18;
            g2.setFont(hint);
            g2.setColor(new Color(170, 255, 190));
            g2.drawString("…나머지는 직접 느껴봐.", x, y);

            g2.dispose();
        }
    }
}
