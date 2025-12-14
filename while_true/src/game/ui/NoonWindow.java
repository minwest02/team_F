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
        // 파일명/경로는 네가 넣어둔 이름에 맞게 수정
        backgroundImage = new ImageIcon("assets/images/noon/00_캠퍼스 배경.png").getImage();

        // 전체 레이아웃
        setLayout(new BorderLayout());

        // ===== 중앙(배경 + NPC) 패널 =====
        JPanel centerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    // 패널 크기에 맞게 배경을 채워 그림
                    g.drawImage(backgroundImage, 0, 0,
                            getWidth(), getHeight(), this);
                }
            }
        };
        centerPanel.setBackground(new Color(245, 245, 248));
        add(centerPanel, BorderLayout.CENTER);

        // ===== 캐릭터를 조금 아래로 내리기 위한 래퍼 패널 =====
        JPanel characterPanel = new JPanel();
        characterPanel.setOpaque(false); // 배경이 비치도록
        characterPanel.setLayout(new BoxLayout(characterPanel, BoxLayout.Y_AXIS));

        // 위쪽에 여백(내리고 싶은 정도만큼 조절 가능)
        int offsetY = 230; // ← 숫자 키우면 더 아래로 내려감
        characterPanel.add(Box.createVerticalStrut(offsetY));

        // 가운데 NPC 이미지를 위한 라벨
        playerLabel = new JLabel("", SwingConstants.CENTER);
        playerLabel.setVerticalAlignment(SwingConstants.TOP);
        playerLabel.setPreferredSize(new Dimension(400, 500));
        playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // 가운데 정렬

        characterPanel.add(playerLabel);

        // centerPanel의 가운데에 이 래퍼 패널을 올림
        centerPanel.add(characterPanel, BorderLayout.CENTER);


        // ===== 하단 전체 루트 패널 =====
        JPanel bottomRoot = new JPanel(new BorderLayout());
        bottomRoot.setPreferredSize(new Dimension(1200, 260));
        bottomRoot.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(bottomRoot, BorderLayout.SOUTH);

        // ----- (1) 상태/대사 영역 -----
        JPanel infoPanel = new JPanel(new BorderLayout());
        bottomRoot.add(infoPanel, BorderLayout.CENTER);

        // 왼쪽: 상태 로그
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        statusArea.setText("현재 상태가 여기 표시됩니다.");

        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setPreferredSize(new Dimension(250, 200));
        infoPanel.add(statusScroll, BorderLayout.WEST);

        // 가운데: NPC 대사
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
        loadNpcIcons();   // 라벨 크기에 맞춰 리사이즈
        setNpcImage(1);   // 시작할 때 1번 NPC 이미지 보여주기
    }

    // ===== 외부에서 쓰는 메서드들 =====

    public JButton getBtn1() { return btnChoice1; }
    public JButton getBtn2() { return btnChoice2; }
    public JButton getBtn3() { return btnChoice3; }

    /** 가운데 대사 영역 텍스트 설정 + 항상 맨 위부터 보이게 */
    public void printDialogue(String text) {
        dialogueArea.setText(text);
        dialogueArea.setCaretPosition(0); // 항상 대사 첫 줄이 보이도록
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

    /**
     * NPC 이미지 파일들을 미리 로딩하는 메서드
     * - playerLabel의 preferredSize(400x500)를 기준으로
     *   비율을 유지하면서 안에 딱 맞게 리사이즈함
     * - 파일명은 01_~, 02_~ 형식으로 맞춰둔 상태 기준
     */
    private void loadNpcIcons() {
        String basePath = "assets/images/noon/";

        // NPC 번호 = 배열 인덱스 + 1
        String[] files = {
                "01_교수님.png",      // 1
                "02_버스기사.png",    // 2
                "03_학교친구.png",    // 3
                "04_선배.png",        // 4
                "05_후배.png",        // 5
                "06_동아리사람.png",  // 6
                "07_헬창.png",        // 7
                "08_식당주인.png",    // 8
                "09_대학원생.png",    // 9
                "10_스님.png",        // 10
                "11_과대표.png",      // 11
                "14_조교.png"         // 12
        };

        // 라벨의 목표 크기(= preferredSize)를 기준으로 스케일링
        Dimension d = playerLabel.getPreferredSize();
        int targetW = d.width;
        int targetH = d.height;

        for (int i = 0; i < files.length; i++) {
            ImageIcon rawIcon = new ImageIcon(basePath + files[i]);
            Image rawImg = rawIcon.getImage();

            int imgW = rawIcon.getIconWidth();
            int imgH = rawIcon.getIconHeight();

            // 가로/세로 비율 유지하면서 라벨 크기 안에 딱 들어가도록 스케일 계산
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

    /** NPC 번호(1~12)에 따라 가운데 playerLabel에 이미지 표시 */
    public void setNpcImage(int npcIndex) {
        if (npcIndex < 1 || npcIndex > npcIcons.length) {
            return;
        }
        playerLabel.setIcon(npcIcons[npcIndex - 1]);
        playerLabel.setText(null);
    }
}
