package game.stage.night;

import javax.swing.JFrame;

public class NightWindow extends JFrame {
	public NightWindow() {
		setTitle("밤 스테이지 - while true");
		setSize(1200, 900);
		add(new NightScreen());
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
