package game.stage.night;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class NightObject implements KeyListener {
	private BufferedImage backgroundImage;
	private BufferedImage wall;
	private BufferedImage MC;
	private BufferedImage Bed;
	private BufferedImage Monster;
	private BufferedImage Phone;
	private BufferedImage Desk;
	private BufferedImage Chair;
	private BufferedImage LED;
	private BufferedImage Heart;
	private BufferedImage HalfHeart;
	private BufferedImage GUI;
	private NightMap loadmap;
	private char[][] map = new char[9][9];
	private NightState loadstate;
	private NightState state;
	private BufferedImage Steps;
	
	public NightObject() {
		loadstate = new NightState();
		state=loadstate;
		loadImage();
		loadMap();
	}
	
	private void loadImage() {
		try {
			this.backgroundImage = ImageIO.read(new File("assets/images/night/NightBackground.png"));
			this.wall = ImageIO.read(new File("assets/images/night/NightWall.png"));
			this.MC = ImageIO.read(new File("assets/images/night/NightHuman.png"));
			this.Bed = ImageIO.read(new File("assets/images/night/NightBed.png"));
			this.Monster = ImageIO.read(new File("assets/images/night/NightMonster.png"));
			this.Phone = ImageIO.read(new File("assets/images/night/NightPhone.png"));
			this.Desk = ImageIO.read(new File("assets/images/night/NightDesk.png"));
			this.Chair = ImageIO.read(new File("assets/images/night/NightChair.png"));
			this.LED = ImageIO.read(new File("assets/images/night/NightLED.png"));
			this.Heart = ImageIO.read(new File("assets/images/night/NightHeart.png"));
			this.HalfHeart = ImageIO.read(new File("assets/images/night/NightHalfHeart.png"));
			this.GUI = ImageIO.read(new File("assets/images/night/NightGUI.png"));
			this.Steps = ImageIO.read(new File("assets/images/night/NightSteps.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadMap() {
		loadmap = new NightMap();
		for(int i=0; i<9; i++) {
			for(int j=0; j<9; j++) {
				map[i][j] = loadmap.map[i][j];
			}
		}
		state.index_x = loadmap.start_index_x;
		state.index_y = loadmap.start_index_y;
		state.moveCount = loadmap.movecount;
		state.x = loadmap.start_x;
		state.y = loadmap.start_y;
	}
	
	public void draw(Graphics g, NightScreen screen) {
		drawObject(map, g, screen);
		drawGUI(g, screen);
	}
	
	public void drawObject(char[][] map, Graphics g, NightScreen screen) {
		g.drawImage(backgroundImage, 150, 0, 900, 900, null);
		for(int i=0; i<9; i++) {
			for(int j=0; j<9; j++) {
				int x = 150 + 100*j;
				int y = 100 * i;
				
				switch(map[i][j]) {
				case 'w':
					g.drawImage(wall, x, y, 100, 100, null);
					break;
				case 'g':
					break;
				case 'h':
					g.drawImage(MC, x, y, 100, 100, null);
					break;
				case 'b':
					g.drawImage(Bed, x, y, 100, 100, null);
					break;
				case 'm':
					g.drawImage(Monster, x, y, 100, 100, null);
					break;
				case 'n':
					g.drawImage(Monster, x, y, 100, 100, null);
					break;
				case 'p':
					g.drawImage(Phone, x, y, 100, 100, null);
					break;
				case 'd':
					g.drawImage(Desk, x, y, 100, 100, null);
					break;
				case 'c':
					g.drawImage(Chair, x, y, 100, 100, null);
					break;
				case 'l':
					g.drawImage(LED, x, y, 100, 100, null);
					break;
				}
			}
		}
	}
	
	public void drawGUI(Graphics g, NightScreen screen) {
		g.drawImage(GUI, 0, 5, 140, 195, null);
		if(state.heartCount == 2) {
			g.drawImage(Heart, 1075, 25, 100, 100, null);
		}
		else if(state.heartCount == 1) {
			g.drawImage(HalfHeart, 1075, 25, 100, 100, null);
		}
		g.drawImage(Steps, 1075, 775, 1075 + 100, 775 + 100, 100 * state.moveCount, 0, 100 * state.moveCount + 100, 100, null);
	}
	
	public void left() {
		switch(map[state.index_y][state.index_x - 1]) {
		case 'w':
			break;
		case 'g':
			map[state.index_y][state.index_x - 1] = 'h';
			map[state.index_y][state.index_x] = 'g';
			state.index_x = state.index_x - 1;
			state.moveCount = state.moveCount - 1;
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'b':
			map[state.index_y][state.index_x - 1] = 'h';
			map[state.index_y][state.index_x] = 'g';
			state.index_x = state.index_x - 1;
			state.moveCount = state.moveCount - 1;
			gameWin();
			break;
		case 'm':
			if(map[state.index_y][state.index_x - 2] == 'w') {
				break;
			}
			else if(map[state.index_y][state.index_x - 2] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y][state.index_x - 2] = 'n';
				map[state.index_y][state.index_x - 1] = 'h';
				state.index_x = state.index_x - 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'n':
			map[state.index_y][state.index_x] = 'g';
			map[state.index_y][state.index_x - 1] = 'h';
			state.index_x = state.index_x - 1;
			state.moveCount = state.moveCount - 1;
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'p':
			if(map[state.index_y][state.index_x - 2] == 'w') {
				break;
			}
			else if(map[state.index_y][state.index_x - 2] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y][state.index_x - 2] = 'p';
				map[state.index_y][state.index_x - 1] = 'h';
				state.index_x = state.index_x - 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'd':
			if(map[state.index_y][state.index_x - 2] == 'w') {
				break;
			}
			else if(map[state.index_y][state.index_x - 2] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y][state.index_x - 2] = 'd';
				map[state.index_y][state.index_x - 1] = 'h';
				state.index_x = state.index_x - 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'c':
			if(map[state.index_y][state.index_x - 2] == 'w') {
				break;
			}
			else if(map[state.index_y][state.index_x - 2] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y][state.index_x - 2] = 'c';
				map[state.index_y][state.index_x - 1] = 'h';
				state.index_x = state.index_x - 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'l':
			if(map[state.index_y][state.index_x - 2] == 'w') {
				break;
			}
			else if(map[state.index_y][state.index_x - 2] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y][state.index_x - 2] = 'l';
				map[state.index_y][state.index_x - 1] = 'h';
				state.index_x = state.index_x - 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		}
	}
	public void right() {
		switch(map[state.index_y][state.index_x + 1]) {
		case 'w':
			break;
		case 'g':
			map[state.index_y][state.index_x + 1] = 'h';
			map[state.index_y][state.index_x] = 'g';
			state.index_x = state.index_x + 1;
			state.moveCount = state.moveCount - 1;
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'b':
			map[state.index_y][state.index_x + 1] = 'h';
			map[state.index_y][state.index_x] = 'g';
			state.index_x = state.index_x + 1;
			state.moveCount = state.moveCount - 1;
			gameWin();
			break;
		case 'm':
			if(map[state.index_y][state.index_x + 2] == 'w') {
				break;
			}
			else if(map[state.index_y][state.index_x + 2] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y][state.index_x + 2] = 'n';
				map[state.index_y][state.index_x + 1] = 'h';
				state.index_x = state.index_x + 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'n':
			map[state.index_y][state.index_x] = 'g';
			map[state.index_y][state.index_x + 1] = 'h';
			state.index_x = state.index_x + 1;
			state.moveCount = state.moveCount - 1;
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'p':
			if(map[state.index_y][state.index_x + 2] == 'w') {
				break;
			}
			else if(map[state.index_y][state.index_x + 2] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y][state.index_x + 2] = 'p';
				map[state.index_y][state.index_x + 1] = 'h';
				state.index_x = state.index_x + 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'd':
			if(map[state.index_y][state.index_x + 2] == 'w') {
				break;
			}
			else if(map[state.index_y][state.index_x + 2] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y][state.index_x + 2] = 'd';
				map[state.index_y][state.index_x + 1] = 'h';
				state.index_x = state.index_x + 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'c':
			if(map[state.index_y][state.index_x + 2] == 'w') {
				break;
			}
			else if(map[state.index_y][state.index_x + 2] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y][state.index_x + 2] = 'c';
				map[state.index_y][state.index_x + 1] = 'h';
				state.index_x = state.index_x + 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'l':
			if(map[state.index_y][state.index_x + 2] == 'w') {
				break;
			}
			else if(map[state.index_y][state.index_x + 2] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y][state.index_x + 2] = 'l';
				map[state.index_y][state.index_x + 1] = 'h';
				state.index_x = state.index_x + 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		}
	}
	public void down() {
		switch(map[state.index_y + 1][state.index_x]) {
		case 'w':
			break;
		case 'g':
			map[state.index_y][state.index_x] = 'g';
			map[state.index_y + 1][state.index_x] = 'h';
			state.index_y = state.index_y + 1;
			state.moveCount = state.moveCount - 1;
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'b':
			map[state.index_y][state.index_x] = 'g';
			map[state.index_y + 1][state.index_x] = 'h';
			state.index_y = state.index_y + 1;
			state.moveCount = state.moveCount - 1;
			gameWin();
			break;
		case 'm':
			if(map[state.index_y + 2][state.index_x] == 'w') {
				break;
			}
			else if(map[state.index_y + 2][state.index_x] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y + 1][state.index_x] = 'h';
				map[state.index_y + 2][state.index_x] = 'n';
				state.index_y = state.index_y + 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'n':
			map[state.index_y][state.index_x] = 'g';
			map[state.index_y + 1][state.index_x] = 'h';
			state.index_y = state.index_y + 1;
			state.moveCount = state.moveCount - 1;
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'p':
			if(map[state.index_y + 2][state.index_x] == 'w') {
				break;
			}
			else if(map[state.index_y + 2][state.index_x] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y + 1][state.index_x] = 'h';
				map[state.index_y + 2][state.index_x] = 'p';
				state.index_y = state.index_y + 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'd':
			if(map[state.index_y + 2][state.index_x] == 'w') {
				break;
			}
			else if(map[state.index_y + 2][state.index_x] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y + 1][state.index_x] = 'h';
				map[state.index_y + 2][state.index_x] = 'd';
				state.index_y = state.index_y + 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'c':
			if(map[state.index_y + 2][state.index_x] == 'w') {
				break;
			}
			else if(map[state.index_y + 2][state.index_x] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y + 1][state.index_x] = 'h';
				map[state.index_y + 2][state.index_x] = 'c';
				state.index_y = state.index_y + 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'l':
			if(map[state.index_y + 2][state.index_x] == 'w') {
				break;
			}
			else if(map[state.index_y + 2][state.index_x] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y + 1][state.index_x] = 'h';
				map[state.index_y + 2][state.index_x] = 'l';
				state.index_y = state.index_y + 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		}
	}
	public void up() {
		switch(map[state.index_y - 1][state.index_x]) {
		case 'w':
			break;
		case 'g':
			map[state.index_y][state.index_x] = 'g';
			map[state.index_y - 1][state.index_x] = 'h';
			state.index_y = state.index_y - 1;
			state.moveCount = state.moveCount - 1;
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'b':
			map[state.index_y][state.index_x] = 'g';
			map[state.index_y - 1][state.index_x] = 'h';
			state.index_y = state.index_y - 1;
			state.moveCount = state.moveCount - 1;
			gameWin();
			break;
		case 'm':
			if(map[state.index_y - 2][state.index_x] == 'w') {
				break;
			}
			else if(map[state.index_y - 2][state.index_x] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y - 1][state.index_x] = 'h';
				map[state.index_y - 2][state.index_x] = 'n';
				state.index_y = state.index_y - 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'n':
			map[state.index_y][state.index_x] = 'g';
			map[state.index_y - 1][state.index_x] = 'h';
			state.index_y = state.index_y - 1;
			state.moveCount = state.moveCount - 1;
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'p':
			if(map[state.index_y - 2][state.index_x] == 'w') {
				break;
			}
			else if(map[state.index_y - 2][state.index_x] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y - 1][state.index_x] = 'h';
				map[state.index_y - 2][state.index_x] = 'p';
				state.index_y = state.index_y - 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'd':
			if(map[state.index_y - 2][state.index_x] == 'w') {
				break;
			}
			else if(map[state.index_y - 2][state.index_x] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y - 1][state.index_x] = 'h';
				map[state.index_y - 2][state.index_x] = 'd';
				state.index_y = state.index_y - 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'c':
			if(map[state.index_y - 2][state.index_x] == 'w') {
				break;
			}
			else if(map[state.index_y - 2][state.index_x] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y - 1][state.index_x] = 'h';
				map[state.index_y - 2][state.index_x] = 'c';
				state.index_y = state.index_y - 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		case 'l':
			if(map[state.index_y - 2][state.index_x] == 'w') {
				break;
			}
			else if(map[state.index_y - 2][state.index_x] == 'g') {
				map[state.index_y][state.index_x] = 'g';
				map[state.index_y - 1][state.index_x] = 'h';
				map[state.index_y - 2][state.index_x] = 'l';
				state.index_y = state.index_y - 1;
				state.moveCount = state.moveCount - 1;
			}
			if(state.moveCount == 0) {
				gameOver();
			}
			break;
		}
	}
	
	public void gameOver() {
		loadMap();
		state=loadstate;
		state.heartCount = 1;
	}
	public void gameWin() {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		switch(e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			left();
			break;
		case KeyEvent.VK_RIGHT:
			right();
			break;
		case KeyEvent.VK_UP:
			up();
			break;
		case KeyEvent.VK_DOWN:
			down();
			break;
		case KeyEvent.VK_R:
			gameOver();
		}
	}
}
