package game.stage.morning;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Snake {

    private final LinkedList<Segment> body = new LinkedList<>();
    private int dx = 1, dy = 0;

    public Snake(Point start) {
        body.add(new Segment(new Point(start), null)); // 머리 item은 항상 null
    }

    public LinkedList<Segment> body() { return body; }

    public Segment head() { return body.getFirst(); }

    public void setDirection(int ndx, int ndy) {
        if (ndx == 0 && ndy == 0) return;
        dx = ndx;
        dy = ndy;
    }

    public void onKey(int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_UP && dy != 1) { dx = 0; dy = -1; }
        if (keyCode == java.awt.event.KeyEvent.VK_DOWN && dy != -1) { dx = 0; dy = 1; }
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT && dx != 1) { dx = -1; dy = 0; }
        if (keyCode == java.awt.event.KeyEvent.VK_RIGHT && dx != -1) { dx = 1; dy = 0; }
    }

    public Point nextHeadPos() {
        Point h = head().pos;
        return new Point(h.x + dx, h.y + dy);
    }

    public boolean hitsBody(Point p) {
        for (int i = 1; i < body.size(); i++) {
            if (body.get(i).pos.equals(p)) return true;
        }
        return false;
    }

    public HashSet<Point> occupied() {
        HashSet<Point> set = new HashSet<>();
        for (Segment s : body) set.add(new Point(s.pos));
        return set;
    }

    public void repositionKeepBody(Point newHead, Point entryDir, Room room) {
        int len = body.size();

        List<ItemType> items = new ArrayList<>();
        for (int i = 1; i < body.size(); i++) items.add(body.get(i).item);

        Point back = new Point(-entryDir.x, -entryDir.y);

        HashSet<Point> reserved = new HashSet<>();
        Point entryLane = new Point(newHead.x - entryDir.x, newHead.y - entryDir.y);
        reserved.add(entryLane);

        LinkedList<Segment> rebuilt = new LinkedList<>();
        HashSet<Point> used = new HashSet<>();

        Segment newHeadSeg = new Segment(new Point(newHead), null);
        rebuilt.add(newHeadSeg);
        used.add(newHeadSeg.pos);

        Point cur = newHeadSeg.pos;

        for (int i = 1; i < len; i++) {
            Point next = pickNextTailCell(cur, back, used, reserved, room);
            if (next == null) break;

            ItemType it = (i - 1 < items.size()) ? items.get(i - 1) : null;
            rebuilt.add(new Segment(new Point(next), it));
            used.add(next);
            cur = next;
        }

        body.clear();
        body.addAll(rebuilt);

        body.getFirst().item = null;
    }

    private Point pickNextTailCell(Point cur, Point back, HashSet<Point> used, HashSet<Point> reserved, Room room) {
        Point[] cands = new Point[] {
                new Point(cur.x + back.x, cur.y + back.y),
                new Point(cur.x + back.y, cur.y - back.x),
                new Point(cur.x - back.y, cur.y + back.x),
                new Point(cur.x - back.x, cur.y - back.y)
        };

        for (Point p : cands) {
            if (!isPlaceable(p, used, reserved, room)) continue;
            return p;
        }

        Point[] around = new Point[] {
                new Point(cur.x + 1, cur.y),
                new Point(cur.x - 1, cur.y),
                new Point(cur.x, cur.y + 1),
                new Point(cur.x, cur.y - 1)
        };
        for (Point p : around) {
            if (!isPlaceable(p, used, reserved, room)) continue;
            return p;
        }

        return null;
    }

    private boolean isPlaceable(Point p, HashSet<Point> used, HashSet<Point> reserved, Room room) {
        if (used.contains(p)) return false;
        if (reserved.contains(p)) return false;
        if (room.isWall(p)) return false;
        if (room.tileAt(p) == TileType.DOOR) return false;
        return true;
    }

    public void move(ItemType pickedItem) {
        Point next = nextHeadPos();

        Point prevTailPos = new Point(body.getLast().pos);

        for (int i = body.size() - 1; i >= 1; i--) {
            body.get(i).pos.setLocation(body.get(i - 1).pos);
        }

        // 머리 이동
        head().pos.setLocation(next);

        if (pickedItem != null) {
            body.addLast(new Segment(prevTailPos, pickedItem));
        }
    }

    public ItemType popLastItem() {
        if (body.size() <= 1) return null;
        Segment last = body.removeLast();
        return last.item;
    }

    public void loseOne() {
        if (body.size() > 1) body.removeLast();
    }

    public void bump() {
        dx = 0;
        dy = 0;
    }
}
