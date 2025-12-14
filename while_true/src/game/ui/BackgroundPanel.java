package game.ui;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;

public class BackgroundPanel extends JPanel {

    private final Image bg;
    private final int reservedBottom; // 하단 UI 영역 높이

    public BackgroundPanel(String relativePath, int reservedBottom) {
        this.reservedBottom = reservedBottom;

        String fullPath = Paths.get(System.getProperty("user.dir"), relativePath).toString();
        bg = new ImageIcon(fullPath).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        int drawH = Math.max(0, h - reservedBottom); // ✅ 하단 reservedBottom 만큼 제외

        if (bg != null && drawH > 0) {
            g.drawImage(bg, 0, 0, w, drawH, this);
        }

        // 하단은 그냥 단색(원하면 색 바꿔도 됨)
        g.setColor(getBackground());
        g.fillRect(0, drawH, w, h - drawH);
    }
}
