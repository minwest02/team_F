package game.stage.morning;

public enum ItemType {
    APPLE("사과", "Apple", new StatDelta(+5, 0, 0, 0)),
    DUMBBELL("아령", "Dumbbell", new StatDelta(+10, 0, 0, 0)),
    COFFEE("커피", "Coffee", new StatDelta(+20, -5, 0, 0)),

    HERB("허브", "Herb", new StatDelta(-5, +5, 0, 0)),
    HEADPHONES("헤드폰", "Headphones", new StatDelta(0, +10, 0, +5)),
    SHIBA("애완동물", "Shiba", new StatDelta(0, +20, 0, 0)),

    PENCIL("연필", "Pencil", new StatDelta(0, 0, +5, 0)),
    BOOK("책", "Book", new StatDelta(0, +5, +10, 0)),
    NOTEBOOK("노트북", "Notebook", new StatDelta(-5, -5, +20, 0)),

    SPEECH_BUBBLE("말풍선", "Speech_bubble", new StatDelta(0, 0, +5, +5)),
    SHOWER_HEAD("샤워", "Shower_head", new StatDelta(+5, +5, 0, +10)),
    POLAROID("사진", "Polaroid", new StatDelta(-5, +5, -5, +20));

    public final String label;
    public final String assetKey;
    public final StatDelta delta;

    ItemType(String label, String assetKey, StatDelta delta) {
        this.label = label;
        this.assetKey = assetKey;
        this.delta = delta;
    }
}
