package game.stage.morning;

import java.awt.Point;

public class Segment {
    public Point pos;
    public ItemType item; // null이면 머리

    public Segment(Point pos, ItemType item) {
        this.pos = pos;
        this.item = item;
    }
}
