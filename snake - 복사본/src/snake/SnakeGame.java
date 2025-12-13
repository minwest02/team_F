import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    static final int TILE_SIZE = 20;
    static final int WIDTH = 30;
    static final int HEIGHT = 30;
    static final int DELAY = 100;

    LinkedList<Point> snake = new LinkedList<>();
    Point food;
    int dx = 1, dy = 0;
    boolean running = true;

    Timer timer;
    Random random = new Random();

    public SnakeGame() {
        setPreferredSize(new Dimension(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        snake.add(new Point(WIDTH / 2, HEIGHT / 2));
        spawnFood();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    void spawnFood() {
        while (true) {
            Point p = new Point(random.nextInt(WIDTH), random.nextInt(HEIGHT));
            if (!snake.contains(p)) {
                food = p;
                return;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running) return;

        Point head = snake.getFirst();
        Point next = new Point(head.x + dx, head.y + dy);

        if (next.x < 0 || next.y < 0 || next.x >= WIDTH || next.y >= HEIGHT || snake.contains(next)) {
            running = false;
            repaint();
            return;
        }

        snake.addFirst(next);

        if (next.equals(food)) {
            spawnFood();
        } else {
            snake.removeLast();
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!running) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", 120, 300);
            return;
        }

        g.setColor(Color.RED);
        g.fillOval(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            g.setColor(i == 0 ? Color.GREEN : new Color(0, 180, 0));
            g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP && dy != 1) { dx = 0; dy = -1; }
        if (key == KeyEvent.VK_DOWN && dy != -1) { dx = 0; dy = 1; }
        if (key == KeyEvent.VK_LEFT && dx != 1) { dx = -1; dy = 0; }
        if (key == KeyEvent.VK_RIGHT && dx != -1) { dx = 1; dy = 0; }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new SnakeGame());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
