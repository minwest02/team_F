package game;

import javax.swing.SwingUtilities;
import game.ui.intro.IntroScene01Overlay;

/**
 * IntroTest
 * - Intro01만 단독으로 띄워서 테스트하는 실행용 클래스
 * - 문제 생기면 여기서만 고치면 됨(본게임 영향 거의 없음)
 */
public class IntroTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new IntroScene01Overlay(() -> {
                System.out.println("Intro01 끝! (다음 씬으로 이어지는 자리)");
                // 다음 씬 연결 테스트하고 싶으면 여기서 호출하면 됨.
                // new game.ui.NoonGuiController();
            }).showOverlay();
        });
    }
}
