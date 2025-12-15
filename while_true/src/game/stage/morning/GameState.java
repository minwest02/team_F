package game.stage.morning;


import java.awt.Point;
import java.util.Random;
import java.util.Set;

public class GameState {

    private final int w, h;
    private final Random rnd = new Random();

    public final Stats stats = new Stats();
    public final Inventory inventory = new Inventory(stats);
    public final Snake snake;

    public Room room;
    private String roomKey;

    private long startMs = System.currentTimeMillis();
    private boolean cleared = false;
    private long lastItemSpawnMs = 0;

    private static final long EXIT_OPEN_MS = 30_000; // 30초

    private boolean exitOpen = false;

    public GameState(int w, int h) {
        this.w = w;
        this.h = h;

        this.roomKey = "personal";
        this.room = RoomFactory.create(roomKey, w, h);

        this.snake = new Snake(new Point(w / 2, h / 2));

        Point spawn = room.entrySpawn(null);
        Point dir = room.entryDir(null);

        snake.repositionKeepBody(spawn, dir, room);
        snake.setDirection(dir.x, dir.y);

        room.ensureItemCount(4, rnd, true, snake.occupied());
    }

    public boolean isCleared() { return cleared; }

    public long elapsedMs() { return System.currentTimeMillis() - startMs; }

    public boolean isExitOpen() { return exitOpen; }

    public long exitRemainingMs() {
        long left = EXIT_OPEN_MS - elapsedMs();
        return Math.max(0, left);
    }

    public void onKey(int keyCode) { snake.onKey(keyCode); }

    public void tick() {
        if (cleared) return;

        long now = System.currentTimeMillis();

        // 출구 오픈
        if (!exitOpen && now - startMs >= EXIT_OPEN_MS) {
            exitOpen = true;
            room.openExitDoor();
        }

        Point next = snake.nextHeadPos();

        // 문 진입 => 방 전환 or 클리어
        if (room.isDoor(next)) {
            String target = room.doorTarget(next);
            if (target != null) {

                if ("exit".equals(target)) {
                    if (exitOpen) cleared = true;
                    return;
                }

                String from = roomKey;

                roomKey = target;
                room = RoomFactory.create(roomKey, w, h);

                if (exitOpen) room.openExitDoor();

                Point spawn = room.entrySpawn(from);
                Point dir = room.entryDir(from);

                snake.repositionKeepBody(spawn, dir, room);
                snake.setDirection(dir.x, dir.y);

                room.ensureItemCount(4, rnd, false, snake.occupied());
            }
            return;
        }

        if (room.isWall(next) || snake.hitsBody(next)) {
            ItemType lost = snake.popLastItem(); // 최근 아이템(꼬리) 제거
            if (lost != null) {
                inventory.removeOne(lost);       // 스탯/인벤 반영
            }
            snake.bump();
            return;
        }

        // 아이템 획득
        ItemType picked = room.pickItemAt(next);
        if (picked != null) {
            inventory.add(picked);
        }

        // 이동(먹었으면 move 안에서 꼬리 세그먼트가 추가됨)
        snake.move(picked);

        // 5초마다 슬롯에서 부족분 채우기
        if (now - lastItemSpawnMs >= 5_000) {
            Set<Point> blocked = snake.occupied();
            room.ensureItemCount(4, rnd, false, blocked);
            lastItemSpawnMs = now;
        }
    }
}
