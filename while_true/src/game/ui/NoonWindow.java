package game.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // ✅ KeyEvent/ActionEvent 포함
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    private final ImageIcon[] npcIcons = new ImageIcon[12];

    // 배경 이미지
    private Image backgroundImage;

    // =========================
    // 리소스 로딩 유틸
    // =========================
    private Image loadImageOrNull(String path) {
        try {
            var url = getClass().getResource(path);
            if (url == null) {
                System.err.println("리소스 못 찾음: " + path);
                return null;
            }
            return new ImageIcon(url).getImage();
        } catch (Exception e) {
            System.err.println("이미지 로드 실패: " + path);
            e.printStackTrace();
            return null;
        }
    }

    private ImageIcon loadIconOrNull(String path) {
        try {
            var url = getClass().getResource(path);
            if (url == null) {
                System.err.println("리소스 못 찾음: " + path);
                return null;
            }
            return new ImageIcon(url);
        } catch (Exception e) {
            System.err.println("아이콘 로드 실패: " + path);
            e.printStackTrace();
            return null;
        }
    }

    // =========================
    // 생성자
    // =========================
    public NoonWindow() {
        setTitle("점심 스테이지 - while true");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ===== 배경 이미지 로딩 (classpath 절대경로) =====
        backgroundImage = loadImageOrNull("/assets/images/noon/00_캠퍼스 배경.png");

        // 전체 레이아웃
        setLayout(new BorderLayout());

        // ===== 중앙(배경 + NPC) 패널 =====
        JPanel centerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // 디버그용(배경 못 찾으면 빨간 화면 + 메시지)
                    g.setColor(Color.RED);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.WHITE);
                    g.drawString("BG IMAGE NULL (resource not found)", 20, 20);
                }
            }
        };
        centerPanel.setOpaque(true);
        add(centerPanel, BorderLayout.CENTER);

        // ===== 캐릭터 위치 조정용 래퍼 패널 =====
        JPanel characterPanel = new JPanel();
        characterPanel.setOpaque(false);
        characterPanel.setLayout(new BoxLayout(characterPanel, BoxLayout.Y_AXIS));

        int offsetY = 230;
        characterPanel.add(Box.createVerticalStrut(offsetY));

        playerLabel = new JLabel("", SwingConstants.CENTER);
        playerLabel.setVerticalAlignment(SwingConstants.TOP);
        playerLabel.setPreferredSize(new Dimension(400, 500));
        playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        characterPanel.add(playerLabel);
        centerPanel.add(characterPanel, BorderLayout.CENTER);

        // ===== 하단 UI =====
        JPanel bottomRoot = new JPanel(new BorderLayout());
        bottomRoot.setPreferredSize(new Dimension(1200, 260));
        bottomRoot.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(bottomRoot, BorderLayout.SOUTH);

        JPanel infoPanel = new JPanel(new BorderLayout());
        bottomRoot.add(infoPanel, BorderLayout.CENTER);

        // 상태 로그
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        statusArea.setText("현재 상태가 여기 표시됩니다.");

        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setPreferredSize(new Dimension(250, 200));
        infoPanel.add(statusScroll, BorderLayout.WEST);

        // 대화 로그
        dialogueArea = new JTextArea();
        dialogueArea.setEditable(false);
        dialogueArea.setLineWrap(true);
        dialogueArea.setWrapStyleWord(true);
        dialogueArea.setFont(new Font("맑은 고딕", Font.PLAIN, 18));

        JScrollPane dialogueScroll = new JScrollPane(dialogueArea);
        infoPanel.add(dialogueScroll, BorderLayout.CENTER);

        // 버튼 영역
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

        // ===== NPC 이미지 로딩 =====
        loadNpcIcons();
        setNpcImage(1);

        // ✅ ESC 확인창 바인딩 (여기서 한번만 설정)
        bindEscConfirm();

        // ===== 창 표시 =====
        setVisible(true);

        // ===== macOS 전면 포커스 + 도움말 오버레이 =====
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setAlwaysOnTop(true);
                toFront();
                requestFocus();
                setAlwaysOnTop(false);

                SwingUtilities.invokeLater(() -> {
                    GameHelpOverlay_Noon.showOnce(NoonWindow.this);
                });
            }
        });
    }

    // =========================
    // 외부에서 쓰는 메서드들
    // =========================
    public JButton getBtn1() { return btnChoice1; }
    public JButton getBtn2() { return btnChoice2; }
    public JButton getBtn3() { return btnChoice3; }

    public void printDialogue(String text) {
        dialogueArea.setText(text);
        dialogueArea.setCaretPosition(0);
    }

    public void setStatusText(String text) {
        statusArea.setText(text);
        statusArea.setCaretPosition(0);
    }

    public void setPlayerLabelText(String text) {
        playerLabel.setIcon(null);
        playerLabel.setText(text);
    }

    // =========================
    // NPC 아이콘 로딩
    // =========================
    private void loadNpcIcons() {
        String basePath = "/assets/images/noon/";

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
            String path = basePath + files[i];

            ImageIcon rawIcon = loadIconOrNull(path);
            if (rawIcon == null || rawIcon.getIconWidth() <= 0 || rawIcon.getIconHeight() <= 0) {
                System.err.println("NPC 아이콘 로드 실패: " + path);
                npcIcons[i] = null;
                continue;
            }

            Image rawImg = rawIcon.getImage();
            int imgW = rawIcon.getIconWidth();
            int imgH = rawIcon.getIconHeight();

            double scale = Math.min((double) targetW / imgW, (double) targetH / imgH);

            int newW = (int) (imgW * scale);
            int newH = (int) (imgH * scale);

            Image scaledImg = rawImg.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            npcIcons[i] = new ImageIcon(scaledImg);
        }
    }

    public void setNpcImage(int npcIndex) {
        if (npcIndex < 1 || npcIndex > npcIcons.length) return;

        ImageIcon icon = npcIcons[npcIndex - 1];
        playerLabel.setIcon(icon);
        playerLabel.setText(null);
    }

    // =========================
    // ESC 확인창 바인딩
    // =========================
    private void bindEscConfirm() {
        JRootPane root = getRootPane();

        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC_CONFIRM");
        am.put("ESC_CONFIRM", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        NoonWindow.this,
                        "정말 넘길까?",
                        "…",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (result == JOptionPane.YES_OPTION) {
                    // 데모: 여기서는 아무 것도 안 함
                    // (원하면 나중에: dispose(); 혹은 컨트롤러 콜백 연결)
                }
            }
        });
    }
}
