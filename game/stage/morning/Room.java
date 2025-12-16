package game.stage.morning;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Room {

    public final int w, h;

    protected final TileType[][] tiles;
    protected final Map<Point, ItemType> items = new HashMap<>();

    protected final String backgroundKey;
    protected final Map<Point, String> doors = new HashMap<>();

    protected final Map<String, Point> entrySpawns = new HashMap<>();
    protected final Map<String, Point> entryDirs = new HashMap<>();

    protected final List<Point> itemSlots = new ArrayList<>();
    protected final List<ItemType> spawnPool = new ArrayList<>();

    // ===== 출구(60초 후 열림) =====
    private Point exitDoorPos = null;
    private boolean exitOpen = false;

    protected Room(int w, int h, String backgroundKey) {
        this.w = w;
        this.h = h;
        this.backgroundKey = backgroundKey;

        tiles = new TileType[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                tiles[x][y] = TileType.FLOOR;
            }
        }
    }

    // ===== 출구 설정/오픈 =====
    public void setExitDoorPos(Point p) {
        this.exitDoorPos = (p == null ? null : new Point(p));
        this.exitOpen = false;
    }

    public boolean hasExitDoor() {
        return exitDoorPos != null;
    }

    public boolean isExitOpen() {
        return exitOpen;
    }

    public void openExitDoor() {
        if (exitOpen) return;
        if (exitDoorPos == null) return;
        if (exitDoorPos.x < 0 || exitDoorPos.y < 0 || exitDoorPos.x >= w || exitDoorPos.y >= h) return;

        tiles[exitDoorPos.x][exitDoorPos.y] = TileType.DOOR;
        doors.put(new Point(exitDoorPos), "exit");
        exitOpen = true;
    }

    // ================== 개인방 ==================
    public static Room personalRoom(int w, int h) {
        Room r = new Room(w, h, "personal_room");

        for (int x = 0; x < w; x++) {
            r.tiles[x][0] = TileType.WALL;
            r.tiles[x][h - 1] = TileType.WALL;
        }
        for (int y = 0; y < h; y++) {
            r.tiles[0][y] = TileType.WALL;
            r.tiles[w - 1][y] = TileType.WALL;
        }

        int doorX = w / 2;

        Point bottom = new Point(doorX, h - 1);
        r.tiles[bottom.x][bottom.y] = TileType.DOOR;
        r.doors.put(bottom, "living");

        int topMax = Math.min(3, h - 1);
        for (int y = 0; y <= topMax; y++) {
            for (int x = 0; x < w; x++) r.tiles[x][y] = TileType.WALL;
        }

        int innerBottom = h - 2;
        if (innerBottom >= 0) {
            for (int x = 0; x < w; x++) r.tiles[x][innerBottom] = TileType.WALL;
            r.tiles[doorX][innerBottom] = TileType.FLOOR;
        }

        r.entrySpawns.put(null, new Point(w / 2, h / 2));
        r.entryDirs.put(null, new Point(0, 1));

        r.entrySpawns.put("living", new Point(doorX, Math.max(1, h - 3)));
        r.entryDirs.put("living", new Point(0, -1));

        // 슬롯(임시)
        r.itemSlots.add(new Point(4, 6));
        r.itemSlots.add(new Point(6, 6));
        r.itemSlots.add(new Point(8, 6));
        r.itemSlots.add(new Point(10, 7));
        r.itemSlots.add(new Point(5, 9));
        r.itemSlots.add(new Point(9, 9));

        // 개인방 스폰: 노트북, 말풍선, 폴라로이드
        r.spawnPool.add(ItemType.NOTEBOOK);
        r.spawnPool.add(ItemType.SPEECH_BUBBLE);
        r.spawnPool.add(ItemType.POLAROID);

        return r;
    }

    // ================== 거실 ==================
    public static Room livingRoom(int w, int h) {
        Room r = new Room(w, h, "living_room");

        for (int x = 0; x < w; x++) {
            r.tiles[x][0] = TileType.WALL;
            r.tiles[x][h - 1] = TileType.WALL;
        }
        for (int y = 0; y < h; y++) {
            r.tiles[0][y] = TileType.WALL;
            r.tiles[w - 1][y] = TileType.WALL;
        }

        int cx = w / 2;
        int cy = h / 2;

        Point top = new Point(cx, 0);
        r.tiles[top.x][top.y] = TileType.DOOR;
        r.doors.put(top, "personal");

        Point left = new Point(0, cy);
        r.tiles[left.x][left.y] = TileType.DOOR;
        r.doors.put(left, "bath");

        Point right = new Point(w - 1, cy);
        r.tiles[right.x][right.y] = TileType.DOOR;
        r.doors.put(right, "study");

        r.entrySpawns.put(null, new Point(cx, cy));
        r.entryDirs.put(null, new Point(0, 1));

        r.entrySpawns.put("personal", new Point(cx, Math.min(h - 2, 2)));
        r.entryDirs.put("personal", new Point(0, 1));

        r.entrySpawns.put("bath", new Point(Math.min(w - 2, 2), cy));
        r.entryDirs.put("bath", new Point(1, 0));

        r.entrySpawns.put("study", new Point(Math.max(1, w - 3), cy));
        r.entryDirs.put("study", new Point(-1, 0));

        // ===== 출구 위치 지정: 거실 아래 중앙(처음엔 벽이라 못 지나감) =====
        r.setExitDoorPos(new Point(cx, h - 1));

        // 슬롯(임시)
        r.itemSlots.add(new Point(4, 4));
        r.itemSlots.add(new Point(6, 4));
        r.itemSlots.add(new Point(8, 5));
        r.itemSlots.add(new Point(10, 6));
        r.itemSlots.add(new Point(5, 9));
        r.itemSlots.add(new Point(9, 10));
        r.itemSlots.add(new Point(11, 9));
        r.itemSlots.add(new Point(7, 8));

        // 거실 스폰: 아령, 시바, 사과
        r.spawnPool.add(ItemType.DUMBBELL);
        r.spawnPool.add(ItemType.SHIBA);
        r.spawnPool.add(ItemType.APPLE);

        return r;
    }

    // ================== 화장실 ==================
    public static Room bathRoom(int w, int h) {
        Room r = new Room(w, h, "bath_room");

        for (int x = 0; x < w; x++) {
            r.tiles[x][0] = TileType.WALL;
            r.tiles[x][h - 1] = TileType.WALL;
        }
        for (int y = 0; y < h; y++) {
            r.tiles[0][y] = TileType.WALL;
            r.tiles[w - 1][y] = TileType.WALL;
        }

        int cy = h / 2;

        Point right = new Point(w - 1, cy);
        r.tiles[right.x][right.y] = TileType.DOOR;
        r.doors.put(right, "living");

        r.entrySpawns.put(null, new Point(w / 2, h / 2));
        r.entryDirs.put(null, new Point(0, 1));

        r.entrySpawns.put("living", new Point(Math.max(1, w - 3), cy));
        r.entryDirs.put("living", new Point(-1, 0));

        // 슬롯(임시)
        r.itemSlots.add(new Point(4, 5));
        r.itemSlots.add(new Point(6, 7));
        r.itemSlots.add(new Point(8, 9));
        r.itemSlots.add(new Point(10, 6));

        // 화장실 스폰: 샤워헤드, 허브
        r.spawnPool.add(ItemType.SHOWER_HEAD);
        r.spawnPool.add(ItemType.HERB);

        return r;
    }

    // ================== 서재 ==================
    public static Room studyRoom(int w, int h) {
        Room r = new Room(w, h, "study_room");

        for (int x = 0; x < w; x++) {
            r.tiles[x][0] = TileType.WALL;
            r.tiles[x][h - 1] = TileType.WALL;
        }
        for (int y = 0; y < h; y++) {
            r.tiles[0][y] = TileType.WALL;
            r.tiles[w - 1][y] = TileType.WALL;
        }

        int cy = h / 2;

        Point left = new Point(0, cy);
        r.tiles[left.x][left.y] = TileType.DOOR;
        r.doors.put(left, "living");

        r.entrySpawns.put(null, new Point(w / 2, h / 2));
        r.entryDirs.put(null, new Point(0, 1));

        r.entrySpawns.put("living", new Point(Math.min(w - 2, 2), cy));
        r.entryDirs.put("living", new Point(1, 0));

        // 슬롯(임시)
        r.itemSlots.add(new Point(5, 5));
        r.itemSlots.add(new Point(7, 6));
        r.itemSlots.add(new Point(9, 7));
        r.itemSlots.add(new Point(6, 9));
        r.itemSlots.add(new Point(10, 10));

        // 서재 스폰: 커피, 연필, 노트북
        r.spawnPool.add(ItemType.COFFEE);
        r.spawnPool.add(ItemType.PENCIL);
        r.spawnPool.add(ItemType.NOTEBOOK);

        return r;
    }

    // ================== 판정 ==================
    public boolean isWall(Point p) {
        if (outOfBounds(p)) return true;
        return tiles[p.x][p.y] == TileType.WALL;
    }

    public boolean isDoor(Point p) {
        if (outOfBounds(p)) return false;
        return tiles[p.x][p.y] == TileType.DOOR;
    }

    public String doorTarget(Point p) { return doors.get(p); }

    public TileType tileAt(Point p) {
        if (outOfBounds(p)) return TileType.WALL;
        return tiles[p.x][p.y];
    }

    private boolean outOfBounds(Point p) {
        return p.x < 0 || p.y < 0 || p.x >= w || p.y >= h;
    }

    public String backgroundKey() { return backgroundKey; }

    public Point entrySpawn(String from) {
        Point p = entrySpawns.get(from);
        if (p != null) return new Point(p);
        p = entrySpawns.get(null);
        if (p != null) return new Point(p);
        return new Point(w / 2, h / 2);
    }

    public Point entryDir(String from) {
        Point d = entryDirs.get(from);
        if (d != null) return new Point(d);
        d = entryDirs.get(null);
        if (d != null) return new Point(d);
        return new Point(0, 1);
    }

    // ================== 아이템 ==================
    public Map<Point, ItemType> items() { return items; }

    public ItemType pickItemAt(Point p) { return items.remove(p); }

    public void ensureItemCount(int target, Random rnd, boolean forceRespawn, Set<Point> blocked) {
        if (forceRespawn) items.clear();
        if (itemSlots.isEmpty()) return;

        int guard = 3000;
        while (items.size() < target && guard-- > 0) {
            Point slot = itemSlots.get(rnd.nextInt(itemSlots.size()));
            Point p = new Point(slot);

            if (tileAt(p) != TileType.FLOOR) continue;
            if (items.containsKey(p)) continue;
            if (blocked != null && blocked.contains(p)) continue;

            items.put(p, randomItemFromPool(rnd));
        }
    }

    private ItemType randomItemFromPool(Random rnd) {
        if (!spawnPool.isEmpty()) {
            return spawnPool.get(rnd.nextInt(spawnPool.size()));
        }
        ItemType[] v = ItemType.values();
        return v[rnd.nextInt(v.length)];
    }
}
