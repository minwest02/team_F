package game.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class BackgroundPanel extends JPanel {

    private final Image bg;
    private final int reservedBottom; // 하단 UI 영역 높이

    public BackgroundPanel(String resourcePath, int reservedBottom) {
        this.reservedBottom = reservedBottom;

        // ✅ src 아래 리소스를 classpath에서 읽기
        bg = new ImageIcon(Objects.requireNonNull(
                getClass().getResource(resourcePath),
                "배경 리소스를 못 찾음: " + resourcePath
        )).getImage();

        setOpaque(true); // 배경은 그려져야 하니까 true 유지
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();
        int drawH = Math.max(0, h - reservedBottom);

        // 배경 이미지 그리기
        g.drawImage(bg, 0, 0, w, drawH, this);

        // 하단 reservedBottom 영역은 투명/단색 중 선택 가능
        // 여기선 그냥 투명처럼 두고 싶으면 아무것도 안 그리면 됨
        // (원하면 아래처럼 살짝 어두운 패널 느낌으로도 가능)
        // g.setColor(new Color(0,0,0,80));
        // g.fillRect(0, drawH, w, h - drawH);
    }
}
