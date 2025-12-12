package game.ui;

import game.stage.evening.EveningGameLogic;

import javax.swing.*;
import java.awt.*;

public class EveningWindow extends JFrame {

    private final EveningGameLogic logic;
    private final EveningGuiController controller;

    private JLabel demonHpLabel;
    private JLabel ammoLabel;
    private JLabel playerHpLabel;
    private JLabel statsLabel;
    private JLabel itemLabel;
    private JTextArea logArea;

    private JButton shootEnemyBtn;
    private JButton shootSelfBtn;

    public EveningWindow(EveningGuiController controller, EveningGameLogic logic) {
        this.controller = controller;
        this.logic = logic;

        setTitle("저녁 스테이지 - 과제 악마 vs 플레이어");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initComponents();
        refreshAll();
        setVisible(true);
    }

    private void initComponents() {
        // 상단
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(1200, 120));

        JLabel demonLabel = new JLabel("과제 악마", SwingConstants.CENTER);
        demonLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        demonHpLabel = new JLabel("HP: 5", SwingConstants.CENTER);
        demonHpLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));

        topPanel.add(demonLabel, BorderLayout.CENTER);
        topPanel.add(demonHpLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // 중앙(총 자리)
        JPanel centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int w = 250, h = 150;
                int x = getWidth() / 2 - w / 2;
                int y = getHeight() / 2 - h / 2;
                g.drawRect(x, y, w, h);
                g.setFont(new Font("맑은 고딕", Font.BOLD, 20));
                g.drawString("총", x + w / 2 - 10, y + h / 2);
            }
        };
        add(centerPanel, BorderLayout.CENTER);

        // 우측(탄 + 버튼)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(220, 900));

        ammoLabel = new JLabel("공포탄: 0발 / 실탄: 0발", SwingConstants.CENTER);
        rightPanel.add(ammoLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        shootEnemyBtn = new JButton("적에게 쏘기");
        shootSelfBtn = new JButton("나에게 쏘기");

        shootEnemyBtn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        shootSelfBtn.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        shootEnemyBtn.addActionListener(e -> controller.onShootEnemy());
        shootSelfBtn.addActionListener(e -> controller.onShootSelf());

        buttonPanel.add(shootEnemyBtn);
        buttonPanel.add(shootSelfBtn);

        rightPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // 하단(상태 + 로그)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(1200, 180));

        JPanel statusPanel = new JPanel(new GridLayout(1, 4));
        JLabel portraitLabel = new JLabel("플레이어 초상화", SwingConstants.CENTER);
        playerHpLabel = new JLabel("내 HP: 4", SwingConstants.CENTER);
        statsLabel = new JLabel("체력: n  멘탈: n  지능: n  사교: n", SwingConstants.CENTER);
        itemLabel = new JLabel("아이템 칸 (4등분 예정)", SwingConstants.CENTER);

        statusPanel.add(portraitLabel);
        statusPanel.add(playerHpLabel);
        statusPanel.add(statsLabel);
        statusPanel.add(itemLabel);

        logArea = new JTextArea(3, 20);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);

        bottomPanel.add(statusPanel, BorderLayout.NORTH);
        bottomPanel.add(logScroll, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshAll() {
        demonHpLabel.setText("HP: " + logic.getDemonHp());
        ammoLabel.setText("공포탄: " + logic.getBlankCount() + "발 / 실탄: " + logic.getLiveCount() + "발");
        playerHpLabel.setText("내 HP: " + logic.getPlayerHp());

        statsLabel.setText(String.format(
                "체력: %d  멘탈: %d  지능: %d  사교: %d",
                logic.getHealth(), logic.getMental(), logic.getIntelligence(), logic.getSocial()
        ));
    }

    public void appendLog(String text) {
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void setButtonsEnabled(boolean enabled) {
        shootEnemyBtn.setEnabled(enabled);
        shootSelfBtn.setEnabled(enabled);
    }
}
