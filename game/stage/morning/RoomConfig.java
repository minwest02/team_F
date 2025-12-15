package game.stage.morning;

import java.util.List;

public class RoomConfig {

    public final int width;
    public final int height;
    public final String backgroundKey;
    public final List<ItemType> spawnItems;

    public RoomConfig(int width, int height, String backgroundKey, List<ItemType> spawnItems) {
        this.width = width;
        this.height = height;
        this.backgroundKey = backgroundKey;
        this.spawnItems = spawnItems;
    }
}
