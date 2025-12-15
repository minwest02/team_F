package game.stage.morning;

import java.util.*;

public class Inventory {

    private final Stats stats;
    private final Map<ItemType, Integer> counts = new EnumMap<>(ItemType.class);

    public Inventory(Stats stats) {
        this.stats = stats;
    }

    public Map<ItemType, Integer> counts() {
        return counts;
    }

    public void add(ItemType type) {
        counts.put(type, counts.getOrDefault(type, 0) + 1);
        stats.apply(type.delta);
    }

    public void removeOne(ItemType type) {
        int c = counts.getOrDefault(type, 0);
        if (c <= 0) return;

        if (c == 1) {
            counts.remove(type);
        } else {
            counts.put(type, c - 1);
        }

        stats.revert(type.delta);
    }

    public ItemType removeRandomOne(Random rnd) {
        ItemType t = pickRandomExisting(rnd);
        if (t == null) return null;
        removeOne(t);
        return t;
    }

    public void removeHalfRandom(Random rnd) {
        int total = totalCount();
        int toRemove = total / 2;
        for (int i = 0; i < toRemove; i++) {
            removeRandomOne(rnd);
        }
    }

    public int totalCount() {
        int sum = 0;
        for (int v : counts.values()) sum += v;
        return sum;
    }

    private ItemType pickRandomExisting(Random rnd) {
        List<ItemType> bag = new ArrayList<>();

        for (Map.Entry<ItemType, Integer> e : counts.entrySet()) {
            for (int i = 0; i < e.getValue(); i++) {
                bag.add(e.getKey());
            }
        }

        if (bag.isEmpty()) return null;
        return bag.get(rnd.nextInt(bag.size()));
    }
}
