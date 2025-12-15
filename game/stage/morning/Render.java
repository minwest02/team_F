package game.stage.morning;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Render {

    public static void draw(Graphics2D g, GameState state, int tile, int w, int h, int uiH) {
        int uiY = h * tile;

        drawBackgroundOrFallback(g, state, tile, w, h);

        drawDoorOverlay(g, state, tile, w, h);

        drawItems(g, state, tile);
        drawSnake(g, state, tile);
        drawUI(g, state, 10, uiY + 25, w * tile, uiH);

        if (state.isCleared()) drawClear(g, w * tile, h * tile);
    }

    private static void drawBackgroundOrFallback(Graphics2D g, GameState state, int tile, int w, int h) {
        Image bg = Assets.get(state.room.backgroundKey());
        if (bg != null) {
            g.drawImage(bg, 0, 0, w * tile, h * tile, null);
            return;
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Point p = new Point(x, y);
                TileType t = state.room.tileAt(p);
                if (t == TileType.WALL) g.setColor(new Color(90, 90, 90));
                else if (t == TileType.DOOR) g.setColor(new Color(160, 120, 60));
                else g.setColor(new Color(200, 200, 180));
                g.fillRect(x * tile, y * tile, tile, tile);
            }
        }
    }

    private static void drawDoorOverlay(Graphics2D g, GameState state, int tile, int w, int h) {
        long now = System.currentTimeMillis();
        double t = (now % 900) / 900.0;

        float alpha = (float)(0.30 + 0.25 * (0.5 + 0.5 * Math.sin(t * Math.PI * 2)));
        Composite oldC = g.getComposite();
        Stroke oldS = g.getStroke();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Point p = new Point(x, y);
                if (state.room.tileAt(p) != TileType.DOOR) continue;

                int px = x * tile;
                int py = y * tile;

                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g.setColor(new Color(255, 240, 150));
                g.fillRect(px, py, tile, tile);

                g.setComposite(oldC);
                g.setColor(new Color(255, 210, 80));
                g.setStroke(new BasicStroke(3f));
                g.drawRect(px + 1, py + 1, tile - 3, tile - 3);

                drawDoorArrow(g, p, tile, w, h);
            }
        }

        g.setStroke(oldS);
        g.setComposite(oldC);
    }

    private static void drawDoorArrow(Graphics2D g, Point p, int tile, int w, int h) {
        int cx = p.x * tile + tile / 2;
        int cy = p.y * tile + tile / 2;

        int dx = 0, dy = 0;
        if (p.y == 0) { dx = 0; dy = -1; }
        else if (p.y == h - 1) { dx = 0; dy = 1; }
        else if (p.x == 0) { dx = -1; dy = 0; }
        else if (p.x == w - 1) { dx = 1; dy = 0; }
        else return;

        int len = tile / 3;
        int ex = cx + dx * len;
        int ey = cy + dy * len;

        g.setColor(new Color(80, 60, 20));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(cx, cy, ex, ey);

        int ax1 = ex + (-dy) * 4 - dx * 4;
        int ay1 = ey + (dx) * 4 - dy * 4;
        int ax2 = ex + (dy) * 4 - dx * 4;
        int ay2 = ey + (-dx) * 4 - dy * 4;

        Polygon head = new Polygon();
        head.addPoint(ex, ey);
        head.addPoint(ax1, ay1);
        head.addPoint(ax2, ay2);
        g.fillPolygon(head);
    }

    private static void drawSnake(Graphics2D g, GameState state, int tile) {
        Image headImg = Assets.get("snake_head"); // snake_head.jpg

        Stroke oldS = g.getStroke();
        Composite oldC = g.getComposite();

        for (int i = 0; i < state.snake.body().size(); i++) {
            Segment s = state.snake.body().get(i);
            int x = s.pos.x * tile;
            int y = s.pos.y * tile;

            if (i == 0) {
                int pad = Math.max(1, tile / 12);
                if (headImg != null) {
                    g.drawImage(headImg, x + pad, y + pad, tile - pad * 2, tile - pad * 2, null);
                } else {
                    g.setColor(new Color(80, 200, 120));
                    g.fillRect(x, y, tile, tile);
                }

                g.setColor(new Color(20, 20, 20));
                g.setStroke(new BasicStroke(3f));
                g.drawRect(x + 1, y + 1, tile - 3, tile - 3);
                continue;
            }

            g.setColor(new Color(120, 180, 120));
            g.fillRoundRect(x + 1, y + 1, tile - 2, tile - 2, 10, 10);

            g.setColor(new Color(30, 60, 30));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(x + 1, y + 1, tile - 3, tile - 3, 10, 10);

            if (s.item != null) {
                Image icon = Assets.get("items/" + s.item.assetKey);
                if (icon != null) {
                    int iconSize = tile / 2;

                    int ix = x + (tile - iconSize) / 2;
                    int iy = y + (tile - iconSize) / 2;

                    g.setColor(new Color(0, 0, 0, 110));
                    g.fillRoundRect(ix - 3, iy - 3, iconSize + 6, iconSize + 6, 10, 10);

                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
                    g.drawImage(icon, ix, iy, iconSize, iconSize, null);
                    g.setComposite(oldC);
                }
            }
        }

        g.setStroke(oldS);
        g.setComposite(oldC);
    }

    private static void drawItems(Graphics2D g, GameState state, int tile) {
        long now = System.currentTimeMillis();

        for (Map.Entry<Point, ItemType> e : state.room.items().entrySet()) {
            Point p = e.getKey();
            ItemType type = e.getValue();

            double phase = ((p.x * 31 + p.y * 17) % 100) / 100.0;
            double tt = (now % 900) / 900.0;
            int bob = (int) Math.round(Math.sin((tt + phase) * Math.PI * 2) * 2.0);

            int x = p.x * tile;
            int y = p.y * tile + bob;

            float pulse = (float)(0.45 + 0.25 * (0.5 + 0.5 * Math.sin((tt + phase) * Math.PI * 2)));
            Composite oldC = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
            g.setColor(new Color(255, 235, 120));
            Stroke oldS = g.getStroke();
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(p.x * tile + 2, p.y * tile + 2, tile - 4, tile - 4, 6, 6);
            g.setStroke(oldS);
            g.setComposite(oldC);

            String key = "items/" + type.assetKey;
            Image img = Assets.get(key);

            if (img != null) {
                int pad = Math.max(2, tile / 8);
                int iw = tile - pad * 2;
                int ih = tile - pad * 2;

                g.drawImage(img, x + pad - 1, y + pad, iw, ih, null);
                g.drawImage(img, x + pad + 1, y + pad, iw, ih, null);
                g.drawImage(img, x + pad, y + pad - 1, iw, ih, null);
                g.drawImage(img, x + pad, y + pad + 1, iw, ih, null);

                g.drawImage(img, x + pad, y + pad, iw, ih, null);
            } else {
                g.setColor(Color.BLACK);
                g.fillOval(x + 5, y + 5, tile - 10, tile - 10);
                g.setColor(Color.RED);
                g.fillOval(x + 6, y + 6, tile - 12, tile - 12);
            }
        }
    }

    private static void drawUI(Graphics2D g, GameState state, int x, int y, int widthPx, int uiH) {
        int top = y - 18;

        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, top, widthPx, uiH);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 14));

        long sec = state.elapsedMs() / 1000;
        g.drawString("Time: " + sec + "s", x, y);

        if (!state.isExitOpen()) {
            long leftSec = (state.exitRemainingMs() + 999) / 1000;
            g.drawString("Exit opens in: " + leftSec + "s (Living room bottom)", x + 140, y);
        } else {
            g.drawString("EXIT OPEN! (Living room bottom)", x + 140, y);
        }

        g.drawString(
                "HP " + state.stats.hp +
                        " | MENT " + state.stats.ment +
                        " | INT " + state.stats.intel +
                        " | SOC " + state.stats.social,
                x, y + 20
        );

        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        g.drawString("Inventory:", x, y + 45);

        List<Map.Entry<ItemType, Integer>> list = new ArrayList<>(state.inventory.counts().entrySet());

        int lineH = 16;
        int col1X = x + 90;
        int col2X = x + 260;

        int startY = y + 45;
        int maxLinesPerCol = (uiH - 55) / lineH;
        if (maxLinesPerCol < 1) maxLinesPerCol = 1;

        int shown = 0;
        for (int i = 0; i < list.size(); i++) {
            int col = i / maxLinesPerCol;
            int row = i % maxLinesPerCol;
            if (col >= 2) break;

            int px = (col == 0) ? col1X : col2X;
            int py = startY + row * lineH;

            Map.Entry<ItemType, Integer> e = list.get(i);
            g.drawString(e.getKey().label + " x" + e.getValue(), px, py);

            shown++;
        }

        if (list.size() > shown) {
            g.drawString("...", col2X, startY + (maxLinesPerCol - 1) * lineH);
        }
    }

    private static void drawClear(Graphics2D g, int w, int h) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, w, h);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 42));
        g.drawString("CLEAR!", w / 2 - 80, h / 2);
    }
}
