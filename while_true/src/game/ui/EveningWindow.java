package game.ui;

import game.stage.evening.EveningGameLogic;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class EveningWindow extends JFrame {

    private final EveningGameLogic logic;
    private final EveningGuiController controller;

    // ===== 상단 악마 HP =====
    private HeartBar demonHeartBar;

    // ===== 중앙 악마/총 =====
    private JLabel demonImageLabel;
    private JLabel shotgunLabel;

    private ImageIcon demonIdleIcon;

    private ImageIcon shotgunIdleIcon;
    private ImageIcon shotgunPlayerShotIcon; // 플레이어가 적에게 쏠 때
    private ImageIcon shotgunDemonShotIcon;  // 악마가(또는 플레이어에게) 쏠 때

    // 격발 애니메이션 상태
    private Timer shotgunAnimTimer;
    private boolean isShotgunAnimating = false;

    // ===== 우측 탄약 HUD =====
    private AmmoHud ammoHud;

    // ===== 하단 플레이어 HUD =====
    private HeartBar playerHeartBar;
    private JLabel statsLabel;
    private JTextArea logArea;

    // 버튼
    private JButton shootEnemyBtn;
    private JButton shootSelfBtn;

    private JButton btnProcrastinate;
    private JButton btnRest;
    private JButton btnGpt;
    private JButton btnRestart;

    private static final int DEMON_MAX_HP = 5;
    private static final int PLAYER_MAX_HP = 5;

    // ✅ 너가 지금 맞춰놓은 값
    private static final int DEMON_SHIFT_X = 300;
    private static final int GUN_SHIFT_X = 350;

    public EveningWindow(EveningGuiController controller, EveningGameLogic logic) {
        this.controller = controller;
        this.logic = logic;

        setTitle("저녁 스테이지 - 과제 악마 vs 플레이어");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);
        setLocationRelativeTo(null);

        // 배경
        setContentPane(new BackgroundPanel("/assets/images/evening/bg_evening.png", 240));
        getContentPane().setLayout(new BorderLayout());

        initComponents();
        refreshAll();

        setVisible(true);
    }

    private void initComponents() {
        Border thickBorder = BorderFactory.createLineBorder(Color.BLACK, 2);

        // =======================
        // 상단: 악마 HP
        // =======================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(1200, 120));
        topPanel.setOpaque(false);

        demonHeartBar = new HeartBar(
                DEMON_MAX_HP,
                "/assets/images/evening/heart_full.png",
                "/assets/images/evening/heart_empty.png"
        );

        JPanel hpWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 24));
        hpWrap.setOpaque(false);
        hpWrap.add(demonHeartBar);

        topPanel.add(hpWrap, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // =======================
        // 중앙: 악마 + 총 (GridBag)
        // =======================
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(-100, 0, 0, 0)); // 필요시 조절

        // 아이콘 로드 (✅ 총은 너가 말한 420x240 고정)
        demonIdleIcon = loadScaledIcon("/assets/images/evening/demon_laptop_idle.png", 420, 280);

        shotgunIdleIcon = loadScaledIcon("/assets/images/evening/shotgun_idle.png", 420, 240);
        shotgunPlayerShotIcon = loadScaledIcon("/assets/images/evening/player_shot_shotgun.png", 420, 240);
        shotgunDemonShotIcon = loadScaledIcon("/assets/images/evening/demon_shot_shotgun.png", 420, 240);

        demonImageLabel = new JLabel(demonIdleIcon);
        demonImageLabel.setOpaque(false);

        shotgunLabel = new JLabel(shotgunIdleIcon);
        shotgunLabel.setOpaque(false);

        // 악마 래퍼(X 이동)
        JPanel demonWrap = new JPanel(new BorderLayout());
        demonWrap.setOpaque(false);

        JPanel leftSpacer = new JPanel();
        leftSpacer.setPreferredSize(new Dimension(DEMON_SHIFT_X, 1));
        leftSpacer.setOpaque(false);

        demonWrap.add(leftSpacer, BorderLayout.WEST);
        demonWrap.add(demonImageLabel, BorderLayout.CENTER);

        // 총 래퍼(X 이동)
        JPanel gunWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gunWrap.setOpaque(false);
        gunWrap.add(Box.createHorizontalStrut(GUN_SHIFT_X));
        gunWrap.add(shotgunLabel);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.CENTER;

        // 악마
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 10, 0);
        centerPanel.add(demonWrap, gc);

        // 총 (악마 아래 붙이기)
        gc.gridy = 1;
        gc.insets = new Insets(-20, 0, 0, 0); // 더 위로 붙이고 싶으면 -30, -40
        centerPanel.add(gunWrap, gc);

        add(centerPanel, BorderLayout.CENTER);

        // =======================
        // 우측: 탄약 HUD + 발사 버튼
        // =======================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 900));
        rightPanel.setOpaque(false);

        ammoHud = new AmmoHud(
                "/assets/images/evening/bullet_blank.png",
                "/assets/images/evening/bullet_live.png"
        );
        rightPanel.add(ammoHud, BorderLayout.NORTH);

        JPanel shootPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        shootPanel.setOpaque(false);

        shootEnemyBtn = new JButton("적에게 쏘기");
        shootSelfBtn = new JButton("나에게 쏘기");

        shootEnemyBtn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        shootSelfBtn.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        // ✅ 버튼 누르면 1초 격발 연출 + 로직 실행
        shootEnemyBtn.addActionListener(e -> {
            playShotgunAnimation(true);   // 플레이어가 적에게 쏘는 컷
            controller.onShootEnemy();
        });

        shootSelfBtn.addActionListener(e -> {
            playShotgunAnimation(false);  // 악마(또는 자기에게) 쏘는 컷
            controller.onShootSelf();
        });

        shootPanel.add(shootEnemyBtn);
        shootPanel.add(shootSelfBtn);

        JPanel shootWrap = new JPanel(new BorderLayout());
        shootWrap.setOpaque(false);
        shootWrap.add(shootPanel, BorderLayout.NORTH);

        rightPanel.add(shootWrap, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // =======================
        // 하단: 3칸(좌/중/우)
        // =======================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(1200, 240));
        bottomPanel.setOpaque(false);

        JPanel bottomGrid = new JPanel(new GridLayout(1, 3, 0, 0));
        bottomGrid.setOpaque(false);

        // 좌: 플레이어 HP/스탯
        JPanel bottomLeft = new JPanel(new GridLayout(3, 1));
        bottomLeft.setOpaque(false);

        playerHeartBar = new HeartBar(
                PLAYER_MAX_HP,
                "/assets/images/evening/heart_full.png",
                "/assets/images/evening/heart_empty.png"
        );

        JPanel playerHpWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        playerHpWrap.setOpaque(false);
        playerHpWrap.add(playerHeartBar);

        statsLabel = new JLabel("체력: ?  멘탈: ?  지능: ?  사교: ?", SwingConstants.CENTER);
        statsLabel.setOpaque(false);

        bottomLeft.add(playerHpWrap);
        bottomLeft.add(new JLabel(""));
        bottomLeft.add(statsLabel);

        // 중: 로그
        JPanel bottomCenter = new JPanel(new BorderLayout());
        bottomCenter.setOpaque(false);

        logArea = new JTextArea(8, 20);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        bottomCenter.add(logScroll, BorderLayout.CENTER);

        // 우: 아이템 2x2
        JPanel bottomRight = new JPanel(new BorderLayout());
        bottomRight.setOpaque(false);

        JPanel itemPanel = new JPanel(new GridLayout(2, 2, 0, 0));
        itemPanel.setOpaque(false);

        btnProcrastinate = new JButton("과제미루기 (0)");
        btnRest = new JButton("잠깐휴식 (0)");
        btnGpt = new JButton("GPT도움 (0)");
        btnRestart = new JButton("재장전 (0)");

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

        bottomLeft.setBorder(thickBorder);
        bottomCenter.setBorder(thickBorder);
        bottomRight.setBorder(thickBorder);

        bottomGrid.add(bottomLeft);
        bottomGrid.add(bottomCenter);
        bottomGrid.add(bottomRight);

        bottomPanel.add(bottomGrid, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // =======================
    // ✅ 1초 격발 연출
    // =======================
    private void playShotgunAnimation(boolean playerShoots) {
        if (isShotgunAnimating) return; // 연타 방지
        isShotgunAnimating = true;

        // 이미지 교체
        shotgunLabel.setIcon(playerShoots ? shotgunPlayerShotIcon : shotgunDemonShotIcon);

        // 버튼 잠깐 잠그고 싶으면(원치 않으면 주석처리 가능)
        setButtonsEnabled(false);

        if (shotgunAnimTimer != null && shotgunAnimTimer.isRunning()) {
            shotgunAnimTimer.stop();
        }

        shotgunAnimTimer = new Timer(1000, e -> {
            shotgunLabel.setIcon(shotgunIdleIcon);

            isShotgunAnimating = false;
            setButtonsEnabled(true);

            shotgunLabel.revalidate();
            shotgunLabel.repaint();
        });
        shotgunAnimTimer.setRepeats(false);
        shotgunAnimTimer.start();
    }

    // ✅ 과제악마가 “자동으로” 쏠 때는 컨트롤러에서 이걸 호출하면 됨
    public void playDemonShotAnimation() {
        playShotgunAnimation(false);
    }

    // ✅ 플레이어가 자동으로 쏘는 연출이 필요하면 이것도 쓸 수 있음
    public void playPlayerShotAnimation() {
        playShotgunAnimation(true);
    }

    // =======================
    // 갱신
    // =======================
    public void refreshAll() {
        demonHeartBar.setHp(logic.getDemonHp());
        playerHeartBar.setHp(logic.getPlayerHp());

        ammoHud.setCounts(logic.getBlankCount(), logic.getLiveCount());

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

    public void appendLog(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void setButtonsEnabled(boolean enabled) {
        shootEnemyBtn.setEnabled(enabled);
        shootSelfBtn.setEnabled(enabled);

        btnProcrastinate.setEnabled(enabled && logic.getItemProcrastinate() > 0);
        btnRest.setEnabled(enabled && logic.getItemRest() > 0);
        btnGpt.setEnabled(enabled && logic.getItemGpt() > 0);
        btnRestart.setEnabled(enabled && logic.getItemRestart() > 0);
    }

    // =======================
    // 유틸
    // =======================
    private ImageIcon loadScaledIcon(String path, int w, int h) {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_FAST);
        return new ImageIcon(img);
    }
}
