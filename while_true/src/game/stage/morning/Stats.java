package game.stage.morning;

public class Stats {
    public int hp = 50;
    public int ment = 50;
    public int intel = 50;
    public int social = 50;

    public void apply(StatDelta d) {
        hp = clamp(hp + d.hp);
        ment = clamp(ment + d.ment);
        intel = clamp(intel + d.intel);
        social = clamp(social + d.social);
    }

    public void revert(StatDelta d) {
        hp = clamp(hp - d.hp);
        ment = clamp(ment - d.ment);
        intel = clamp(intel - d.intel);
        social = clamp(social - d.social);
    }

    private int clamp(int v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }
}
