package game;

import game.core.Game;

/**
 * Main 클래스
 * - 자바 프로그램의 진입점(main 메서드)을 제공함.
 * - 여기서 Game 객체를 생성하고 start()를 호출해 게임을 실행함.
 */
public class Main {

    /** 프로그램이 시작될 때 가장 먼저 실행되는 메서드 */
    public static void main(String[] args) {
        Game game = new Game(); // Game 객체 생성
        game.start();           // 게임 시작
    }
}
