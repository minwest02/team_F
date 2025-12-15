package game.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 점심 스테이지 전용 GUI 창
 * - 1200x900
 * - 위쪽: 배경 + NPC 이미지
 * - 아래쪽: 상태 로그 / 대화 / 버튼
 */
public class NoonWindow extends JFrame {

    private JTextArea statusArea;    // 왼쪽: 주인공 상태 로그
    private JTextArea dialogueArea;  // 가운데: N회차 + NPC 대사

    // 가운데 큰 NPC 이미지
    private JLabel playerLabel;

    private JButton btnChoice1;
    private JButton btnChoice2;
    private JButton btnChoice3;

    // NPC 이미지 아이콘 배열 (12명)
    private ImageIcon[] npcIcons = new ImageIcon[12];

    // 배경 이미지
    private Image backgroundImage;

    public NoonWindow() {
        setTitle("점심 스테이지 - while true");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ===== 배경 이미지 로딩 =====
        backgroundImage = new ImageIcon("assets/images/noon/00_캠퍼스 배경.png").getImage();

        // 전체 레이아웃
        setLayout(new BorderLayout());

        // ===== 중앙(배경 + NPC) 패널 =====
        JPanel centerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0,
                            getWidth(), getHeight(), this);
                }
            }
        };
        centerPanel.setBackground(new Color(245, 245, 248));
        add(centerPanel, BorderLayout.CENTER);

        // ===== 캐릭터를 조금 아래로 내리기 위한 래퍼 패널 =====
        JPanel characterPanel = new JPanel();
        characterPanel.setOpaque(false);
        characterPanel.setLayout(new BoxLayout(characterPanel, BoxLayout.Y_AXIS));

        int offsetY = 230; // 숫자 키우면 더 아래로 내려감
        characterPanel.add(Box.createVerticalStrut(offsetY));

        playerLabel = new JLabel("", SwingConstants.CENTER);
        playerLabel.setVerticalAlignment(SwingConstants.TOP);
        playerLabel.setPreferredSize(new Dimension(400, 500));
        playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        characterPanel.add(playerLabel);
        centerPanel.add(characterPanel, BorderLayout.CENTER);

        // ===== 하단 전체 루트 패널 =====
        JPanel bottomRoot = new JPanel(new BorderLayout());
        bottomRoot.setPreferredSize(new Dimension(1200, 260));
        bottomRoot.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(bottomRoot, BorderLayout.SOUTH);

        // ----- (1) 상태/대사 영역 -----
        JPanel infoPanel = new JPanel(new BorderLayout());
        bottomRoot.add(infoPanel, BorderLayout.CENTER);

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        statusArea.setText("현재 상태가 여기 표시됩니다.");

        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setPreferredSize(new Dimension(250, 200));
        infoPanel.add(statusScroll, BorderLayout.WEST);

        dialogueArea = new JTextArea();
        dialogueArea.setEditable(false);
        dialogueArea.setLineWrap(true);
        dialogueArea.setWrapStyleWord(true);
        dialogueArea.setFont(new Font("맑은 고딕", Font.PLAIN, 18));

        JScrollPane dialogueScroll = new JScrollPane(dialogueArea);
        infoPanel.add(dialogueScroll, BorderLayout.CENTER);

        // ----- (2) 버튼 영역 -----
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        btnChoice1 = new JButton("대화한다");
        btnChoice2 = new JButton("적당히 넘긴다");
        btnChoice3 = new JButton("무시한다");

        Dimension btnSize = new Dimension(200, 35);
        btnChoice1.setPreferredSize(btnSize);
        btnChoice2.setPreferredSize(btnSize);
        btnChoice3.setPreferredSize(btnSize);

        buttonPanel.add(btnChoice1);
        buttonPanel.add(btnChoice2);
        buttonPanel.add(btnChoice3);

        bottomRoot.add(buttonPanel, BorderLayout.SOUTH);

        // ===== NPC 이미지 로딩 + 시작 시 1번 NPC 표시 =====
        loadNpcIcons();
        setNpcImage(1);
    }

    // ===== 외부에서 쓰는 메서드들 =====

    public JButton getBtn1() { return btnChoice1; }
    public JButton getBtn2() { return btnChoice2; }
    public JButton getBtn3() { return btnChoice3; }

    // ✅ 추가: 컨트롤러에서 버튼 잠금/해제할 수 있게 함
    public void setButtonsEnabled(boolean enabled) {
        btnChoice1.setEnabled(enabled);
        btnChoice2.setEnabled(enabled);
        btnChoice3.setEnabled(enabled);
    }

    /** 가운데 대사 영역 텍스트 설정 + 항상 맨 위부터 보이게 */
    public void printDialogue(String text) {
        dialogueArea.setText(text);
        dialogueArea.setCaretPosition(0);
    }

    /** 왼쪽 상태 로그 영역 텍스트 설정 */
    public void setStatusText(String text) {
        statusArea.setText(text);
        statusArea.setCaretPosition(0);
    }

    /** 텍스트만 쓰고 싶을 때 (아이콘 제거 + 텍스트 표시) */
    public void setPlayerLabelText(String text) {
        playerLabel.setIcon(null);
        playerLabel.setText(text);
    }

    private void loadNpcIcons() {
        String basePath = "assets/images/noon/";

        String[] files = {
                "01_교수님.png",
                "02_버스기사.png",
                "03_학교친구.png",
                "04_선배.png",
                "05_후배.png",
                "06_동아리사람.png",
                "07_헬창.png",
                "08_식당주인.png",
                "09_대학원생.png",
                "10_스님.png",
                "11_과대표.png",
                "14_조교.png"
        };

        Dimension d = playerLabel.getPreferredSize();
        int targetW = d.width;
        int targetH = d.height;

        for (int i = 0; i < files.length; i++) {
            ImageIcon rawIcon = new ImageIcon(basePath + files[i]);
            Image rawImg = rawIcon.getImage();

            int imgW = rawIcon.getIconWidth();
            int imgH = rawIcon.getIconHeight();

            double scale = Math.min(
                    (double) targetW / imgW,
                    (double) targetH / imgH
            );

            int newW = (int) (imgW * scale);
            int newH = (int) (imgH * scale);

            Image scaledImg = rawImg.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            npcIcons[i] = new ImageIcon(scaledImg);
        }
    }

    public void setNpcImage(int npcIndex) {
        if (npcIndex < 1 || npcIndex > npcIcons.length) {
            return;
        }
        playerLabel.setIcon(npcIcons[npcIndex - 1]);
        playerLabel.setText(null);
    }
}
