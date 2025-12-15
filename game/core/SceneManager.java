package game.core;

/**
 * SceneManager 클래스
 * - 현재 어떤 Scene이 활성화되어 있는지 관리함.
 * - 장면 전환(setScene)과, 현재 장면의 update/render/handleInput 호출을 담당함.
 */
public class SceneManager {

    /** 지금 화면에 표시되고 있는 장면을 가리키는 변수 */
    private Scene currentScene;

    /**
     * 현재 장면을 변경하는 메서드
     * - 새 Scene을 주입받아서 currentScene에 저장하고
     * - 새 장면의 init()을 한 번 호출해 초기화함.
     */
    public void setScene(Scene scene) {
        this.currentScene = scene;
        if (scene != null) {
            scene.init();
        }
    }

    /** 현재 장면의 게임 로직을 한 프레임만큼 업데이트함. */
    public void update() {
        if (currentScene != null) {
            currentScene.update();
        }
    }

    /** 현재 장면을 화면에 그리는 메서드. */
    public void render() {
        if (currentScene != null) {
            currentScene.render();
        }
    }

    /** 현재 장면에서 입력을 처리하도록 넘겨주는 메서드. */
    public void handleInput() {
        if (currentScene != null) {
            currentScene.handleInput();
        }
    }
}
