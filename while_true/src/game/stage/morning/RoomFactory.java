package game.stage.morning;

public class RoomFactory {

    public static Room create(String key, int w, int h) {
        if (key == null) key = "personal";

        switch (key) {
            case "personal":
                return Room.personalRoom(w, h);
            case "living":
                return Room.livingRoom(w, h);
            case "bath":
                return Room.bathRoom(w, h);
            case "study":
                return Room.studyRoom(w, h);
            default:
                return Room.livingRoom(w, h);
        }
    }
}
