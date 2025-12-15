package game.core;

import game.stage.noon.NoonScene; // 점심 스테이지 테스트용

/**
 * Game 클래스
 * - 지금은 점심 스테이지(미연시)만 테스트하기 위한 간단 버전임.
 */
public class Game {

    /** 장면을 관리하는 매니저 객체 */
    private final SceneManager sceneManager = new SceneManager();

    /**
     * 게임을 시작하는 메서드
     * - 현재는 NoonScene(점심 스테이지) 한 판만 실행시키는 구조임.
     */
    public void start() {
        // 점심 스테이지 설정
        sceneManager.setScene(new NoonScene());

        // 점심 스테이지 전체를 한 번에 진행 (NoonScene.update() 안에서 대화 루프가 돌아감)
        sceneManager.update();

        System.out.println("=== 점심 스테이지 종료 ===");
    }
}
