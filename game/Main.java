package game;


import game.ui.title.TitleController;

import javax.swing.SwingUtilities;

/**
 * Main 클래스 (GUI 전용)
 * - 프로그램 시작 시 타이틀 화면을 띄움
 */
public class Main {

    public static void main(String[] args) {
        // Swing GUI는 반드시 EDT에서 실행
        SwingUtilities.invokeLater(() -> {
            new TitleController().show();
        });
    }
}
