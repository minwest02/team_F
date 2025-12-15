package game.ui.intro;

import javax.swing.SwingUtilities;

/**
 * IntroSequenceRunner
 *
 * [역할]
 * - IntroScene01~05 Overlay를 순서대로 "연속 재생"함
 * - 마지막 Intro5가 끝나면 onAllFinished.run() 실행
 *
 * [한줄 요약]
 * - Intro 1→2→3→4→5를 안전하게 이어주는 전용 러너임.
 */
public class IntroSequenceRunner {

    private IntroSequenceRunner() {}

    /**
     * 인트로 1~5를 순서대로 실행한 뒤, 전부 끝나면 onAllFinished를 호출함.
     */
    public static void start(Runnable onAllFinished) {
        // ✅ Swing 스레드에서 시작 보장
        SwingUtilities.invokeLater(() -> play01(() -> play02(() -> play03(() -> play04(() -> play05(onAllFinished))))));
    }

    private static void play01(Runnable next) {
        IntroScene01Overlay overlay = new IntroScene01Overlay(() -> safeNext(next));
        overlay.showOverlay();
    }

    private static void play02(Runnable next) {
        IntroScene02Overlay overlay = new IntroScene02Overlay(() -> safeNext(next));
        overlay.showOverlay();
    }

    private static void play03(Runnable next) {
        IntroScene03Overlay overlay = new IntroScene03Overlay(() -> safeNext(next));
        overlay.showOverlay();
    }

    private static void play04(Runnable next) {
        IntroScene04Overlay overlay = new IntroScene04Overlay(() -> safeNext(next));
        overlay.showOverlay();
    }

    private static void play05(Runnable next) {
        IntroScene05Overlay overlay = new IntroScene05Overlay(() -> safeNext(next));
        overlay.showOverlay();
    }

    /**
     * 다음 씬 실행을 Swing 이벤트큐에 한 번 더 올려서
     * "점멸/타이밍 꼬임" 같은 현상을 줄임.
     */
    private static void safeNext(Runnable next) {
        if (next == null) return;
        SwingUtilities.invokeLater(next);
    }
}
