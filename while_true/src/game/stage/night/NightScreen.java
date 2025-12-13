package game.stage.night;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;

public class NightScreen extends Canvas implements ComponentListener {	
	private Graphics bg;
	private Image offScreen;
	private Dimension dim;
	private NightObject object = new NightObject();
	private int countNumber = 0;
	
	public NightScreen() {
		addComponentListener(this);
		addKeyListener(object);
		setFocusable(true);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				repaint();
				counting();
			}			
		}, 0, 1);
		
	}
	
	public void counting() {
		this.countNumber++;
	}
	
	public int getCount() {
		return this.countNumber;
	}
	
	private void initBuffer() {
		this.dim = getSize();
		this.offScreen = createImage(dim.width, dim.height);
		this.bg = this.offScreen.getGraphics();
	}

	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		bg.clearRect(0, 0, dim.width, dim.height);
		object.draw(bg, this);
		g.drawImage(offScreen, 0, 0, this);
		
	}

	@Override
	public void update(Graphics g) {
		// TODO Auto-generated method stub
		paint(g);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		initBuffer();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	

}
