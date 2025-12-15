package game.ui;

import game.stage.evening.EveningGameLogic;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class EveningWindow extends JFrame {

    private final EveningGameLogic logic;
    private final EveningGuiController controller;

    private JLabel demonHpLabel;
    private JLabel ammoLabel;
    private JLabel playerHpLabel;
    private JLabel statsLabel;
    private JTextArea logArea;

    private JButton shootEnemyBtn;
    private JButton shootSelfBtn;

    // 아이템 버튼 4개(2x2)
    private JButton btnProcrastinate;
    private JButton btnRest;
    private JButton btnGpt;
    private JButton btnRestart;

    public EveningWindow(EveningGuiController controller, EveningGameLogic logic) {
        this.controller = controller;
        this.logic = logic;

        setTitle("저녁 스테이지 - 과제 악마 vs 플레이어");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);
        setLocationRelativeTo(null);

        // ✅ 배경 패널을 contentPane으로 교체 (assets/bg/bg_evening.png 사용)
        setContentPane(new BackgroundPanel("assets/bg/bg_evening.png", 240));
        getContentPane().setLayout(new BorderLayout());

        initComponents();
        refreshAll();
        setVisible(true);
    }

    private void initComponents() {
        // 공통 테두리(두께 통일)
        Border thickBorder = BorderFactory.createLineBorder(Color.BLACK, 2);

        // =======================
        // 상단: 과제 악마 + HP
        // =======================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(1200, 120));
        topPanel.setOpaque(false); // ✅ 배경 보이게

        JLabel demonLabel = new JLabel("과제 악마", SwingConstants.CENTER);
        demonLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        demonHpLabel = new JLabel("HP: 5", SwingConstants.CENTER);
        demonHpLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));

        topPanel.add(demonLabel, BorderLayout.CENTER);
        topPanel.add(demonHpLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // =======================
        // 중앙: 총 자리(임시 도형)
        // =======================
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
        centerPanel.setOpaque(false); // ✅ 배경 보이게
        add(centerPanel, BorderLayout.CENTER);

        // =======================
        // 우측: 탄 + 발사 버튼만 (아이템은 하단으로 이동)
        // =======================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 900));
        rightPanel.setOpaque(false); // ✅ 배경 보이게

        ammoLabel = new JLabel("공포탄: 0발 / 실탄: 0발", SwingConstants.CENTER);
        rightPanel.add(ammoLabel, BorderLayout.NORTH);

        JPanel shootPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        shootPanel.setOpaque(false); // ✅ 배경 보이게

        shootEnemyBtn = new JButton("적에게 쏘기");
        shootSelfBtn  = new JButton("나에게 쏘기");

        shootEnemyBtn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        shootSelfBtn.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        shootEnemyBtn.addActionListener(e -> controller.onShootEnemy());
        shootSelfBtn.addActionListener(e -> controller.onShootSelf());

        shootPanel.add(shootEnemyBtn);
        shootPanel.add(shootSelfBtn);

        // 발사 버튼을 우측 하단에 붙여서 위쪽에 떠보이게
        JPanel shootWrap = new JPanel(new BorderLayout());
        shootWrap.setOpaque(false); // ✅ 배경 보이게
        shootWrap.add(shootPanel, BorderLayout.NORTH);

        rightPanel.add(shootWrap, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // =======================
        // 하단: 3칸(좌/중/우) + 경계선 통일
        // =======================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(1200, 240));
        bottomPanel.setOpaque(false); // ✅ 배경 보이게

        JPanel bottomGrid = new JPanel(new GridLayout(1, 3, 0, 0));
        bottomGrid.setPreferredSize(new Dimension(1200, 240));
        bottomGrid.setOpaque(false); // ✅ 배경 보이게

        // (1) 왼쪽: 플레이어/HP/스탯
        JPanel bottomLeft = new JPanel(new GridLayout(3, 1));
        bottomLeft.setOpaque(false); // ✅ 배경 보이게

        JLabel portraitLabel = new JLabel("내 hp 표시(이미지 나중에)", SwingConstants.CENTER);
        playerHpLabel = new JLabel("내 HP: 4", SwingConstants.CENTER);
        statsLabel = new JLabel("체력: n  멘탈: n  지능: n  사교: n", SwingConstants.CENTER);

        bottomLeft.add(portraitLabel);
        bottomLeft.add(playerHpLabel);
        bottomLeft.add(statsLabel);

        // (2) 가운데: 시스템 로그
        JPanel bottomCenter = new JPanel(new BorderLayout());
        bottomCenter.setOpaque(false); // ✅ 바깥 배경은 비치게, 내부 logArea는 흰 박스 그대로

        logArea = new JTextArea(8, 20);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createEmptyBorder()); // ✅ 두께 들쭉날쭉 방지

        bottomCenter.add(logScroll, BorderLayout.CENTER);

        // (3) 오른쪽: 아이템 2x2
        JPanel bottomRight = new JPanel(new BorderLayout());
        bottomRight.setOpaque(false); // ✅ 배경 보이게

        JPanel itemPanel = new JPanel(new GridLayout(2, 2, 0, 0));
        itemPanel.setOpaque(false); // ✅ 배경 보이게

        btnProcrastinate = new JButton("과제미루기 (0)");
        btnRest         = new JButton("잠깐휴식 (0)");
        btnGpt          = new JButton("GPT도움 (0)");
        btnRestart      = new JButton("재장전 (0)");

        // 얇은 내부 경계선 (1px)
        Border thinBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
        btnProcrastinate.setBorder(thinBorder);
        btnRest.setBorder(thinBorder);
        btnGpt.setBorder(thinBorder);
        btnRestart.setBorder(thinBorder);

        btnProcrastinate.addActionListener(e -> controller.onUseProcrastinate());
        btnRest.addActionListener(e -> controller.onUseRest());
        btnGpt.addActionListener(e -> controller.onUseGpt());
        btnRestart.addActionListener(e -> controller.onUseRestart());

        itemPanel.add(btnProcrastinate);
        itemPanel.add(btnRest);
        itemPanel.add(btnGpt);
        itemPanel.add(btnRestart);

        bottomRight.add(itemPanel, BorderLayout.CENTER);

        // ====== 경계선(두께 통일) ======
        bottomLeft.setBorder(thickBorder);
        bottomCenter.setBorder(thickBorder);
        bottomRight.setBorder(thickBorder);

        bottomGrid.add(bottomLeft);
        bottomGrid.add(bottomCenter);
        bottomGrid.add(bottomRight);

        bottomPanel.add(bottomGrid, BorderLayout.CENTER);
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

        refreshItems();
    }

    private void refreshItems() {
        btnProcrastinate.setText("과제미루기 (" + logic.getItemProcrastinate() + ")");
        btnRest.setText("잠깐휴식 (" + logic.getItemRest() + ")");
        btnGpt.setText("GPT도움 (" + logic.getItemGpt() + ")");
        btnRestart.setText("재장전 (" + logic.getItemRestart() + ")");

        btnProcrastinate.setEnabled(logic.getItemProcrastinate() > 0);
        btnRest.setEnabled(logic.getItemRest() > 0);
        btnGpt.setEnabled(logic.getItemGpt() > 0);
        btnRestart.setEnabled(logic.getItemRestart() > 0);
    }

    public void appendLog(String text) {
        logArea.append(text);
        if (!text.endsWith("\n")) logArea.append("\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /** 전체 버튼 on/off (악마 연출/재장전 중에 사용) */
    public void setButtonsEnabled(boolean enabled) {
        shootEnemyBtn.setEnabled(enabled);
        shootSelfBtn.setEnabled(enabled);

        if (!enabled) {
            btnProcrastinate.setEnabled(false);
            btnRest.setEnabled(false);
            btnGpt.setEnabled(false);
            btnRestart.setEnabled(false);
        } else {
            refreshItems();
        }
    }
}
