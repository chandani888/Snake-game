import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {
  private final int TILE_SIZE = 25;
  private final int GRID_SIZE = 20;
  private final int SCREEN_SIZE = GRID_SIZE * TILE_SIZE;

  private final int[] x = new int[GRID_SIZE * GRID_SIZE];
  private final int[] y = new int[GRID_SIZE * GRID_SIZE];

  private int snakeLength;
  private int appleX, appleY, bigAppleX, bigAppleY;
  private boolean bigApplePresent = false;
  private int points;
  private boolean running = false;
  private char direction = 'R';

  private Timer timer;
  private Random random;
  private int gameSpeed = 150; // Default medium speed

  private static int highestScore = 0;
  private static final String HIGH_SCORE_FILE = "highest_score.txt";

  // New variable for snake color
  private Color snakeColor = new Color(34, 139, 34); // Default snake color (dark green)

  public SnakeGame() {
    random = new Random();
    loadHighestScore();
    setPreferredSize(new Dimension(SCREEN_SIZE, SCREEN_SIZE));
    setBackground(Color.BLACK);
    setFocusable(true);

    // Show difficulty selection screen
    showDifficultyMenu();

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (!running) {
          if (e.getKeyCode() == KeyEvent.VK_N) {
            startGame(gameSpeed);
            repaint();
          }
          return;
        }
        switch (e.getKeyCode()) {
          case KeyEvent.VK_LEFT:
            if (direction != 'R')
              direction = 'L';
            break;
          case KeyEvent.VK_RIGHT:
            if (direction != 'L')
              direction = 'R';
            break;
          case KeyEvent.VK_UP:
            if (direction != 'D')
              direction = 'U';
            break;
          case KeyEvent.VK_DOWN:
            if (direction != 'U')
              direction = 'D';
            break;
        }
      }
    });
  }

  // Show difficulty selection menu
  private void showDifficultyMenu() {
    String[] options = { "Easy", "Medium", "Hard", "Change Snake Color" };
    int choice = JOptionPane.showOptionDialog(this,
        "Select Difficulty Level",
        "Snake Game",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.PLAIN_MESSAGE,
        null,
        options,
        options[1]);

    // Handle color change option
    if (choice == 3) {
      changeSnakeColor(); // Change the snake color
      showDifficultyMenu(); // Reopen the difficulty menu after color change
      return;
    }

    // Set game speed based on user choice
    switch (choice) {
      case 0: // Easy
        gameSpeed = 200;
        break;
      case 1: // Medium
        gameSpeed = 150;
        break;
      case 2: // Hard
        gameSpeed = 100;
        break;
      default:
        gameSpeed = 150; // Default to medium if no choice
        break;
    }

    // Start the game with the chosen difficulty
    startGame(gameSpeed);
  }

  // Method to allow the user to change the snake color
  private void changeSnakeColor() {
    Color selectedColor = JColorChooser.showDialog(this, "Choose Snake Color", snakeColor);
    if (selectedColor != null) {
      snakeColor = selectedColor; // Set the selected color
    }
  }

  private void startGame(int delay) {
    if (timer != null) {
      timer.stop();
    }
    snakeLength = 3;
    points = 0;
    for (int i = 0; i < snakeLength; i++) {
      x[i] = (GRID_SIZE / 2 - i) * TILE_SIZE;
      y[i] = (GRID_SIZE / 2) * TILE_SIZE;
    }
    spawnApple();
    running = true;
    timer = new Timer(gameSpeed, this);
    timer.start();
  }

  private void spawnApple() {
    boolean validPosition;
    do {
      validPosition = true;
      appleX = random.nextInt(GRID_SIZE) * TILE_SIZE;
      appleY = random.nextInt(GRID_SIZE) * TILE_SIZE;
      for (int i = 0; i < snakeLength; i++) {
        if (appleX == x[i] && appleY == y[i]) {
          validPosition = false;
          break;
        }
      }
    } while (!validPosition);

    // Occasionally spawn a bigger apple
    if (random.nextInt(5) == 0 && !bigApplePresent) {
      do {
        validPosition = true;
        bigAppleX = random.nextInt(GRID_SIZE) * TILE_SIZE;
        bigAppleY = random.nextInt(GRID_SIZE) * TILE_SIZE;
        for (int i = 0; i < snakeLength; i++) {
          if (bigAppleX == x[i] && bigAppleY == y[i]) {
            validPosition = false;
            break;
          }
        }
      } while (!validPosition);
      bigApplePresent = true;
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (running) {
      // Draw regular apple with leaf
      g.setColor(Color.RED);
      g.fillOval(appleX, appleY, TILE_SIZE, TILE_SIZE);
      g.setColor(Color.GREEN);
      g.fillRect(appleX + TILE_SIZE / 3, appleY - TILE_SIZE / 4, TILE_SIZE / 4, TILE_SIZE / 4);

      // Draw bigger apple if present
      if (bigApplePresent) {
        g.setColor(Color.ORANGE);
        g.fillOval(bigAppleX, bigAppleY, TILE_SIZE * 2, TILE_SIZE * 2);
        g.setColor(Color.GREEN);
        g.fillRect(bigAppleX + TILE_SIZE, bigAppleY - TILE_SIZE / 2, TILE_SIZE / 2, TILE_SIZE / 2);
      }

      // Draw snake without wave pattern for performance
      for (int i = 0; i < snakeLength; i++) {
        if (i == 0) {
          g.setColor(snakeColor); // Head with selected color
        } else {
          g.setColor(new Color(50 + i * 2, 205, 50)); // Lighter green for body segments
        }
        g.fillOval(x[i], y[i], TILE_SIZE, TILE_SIZE);
      }

      // Display points and highest score
      g.setColor(Color.WHITE);
      g.setFont(new Font("Arial", Font.BOLD, 14));
      g.drawString("Points: " + points, 10, 20);
      g.drawString("Highest Score: " + highestScore, 10, 40);
    } else {
      showGameOver(g);
    }
  }

  private void showGameOver(Graphics g) {
    g.setColor(Color.RED);
    g.setFont(new Font("Arial", Font.BOLD, 40));
    FontMetrics metrics = getFontMetrics(g.getFont());
    String message = "Game Over";
    g.drawString(message, (SCREEN_SIZE - metrics.stringWidth(message)) / 2, SCREEN_SIZE / 3);

    // Display points and highest score
    g.setFont(new Font("Arial", Font.PLAIN, 20));
    if (points > 0) {
      g.drawString("Points: " + points, (SCREEN_SIZE - metrics.stringWidth("Points: " + points)) / 2, SCREEN_SIZE / 2);
    } else {
      g.drawString("Better Luck Next Time!", (SCREEN_SIZE - metrics.stringWidth("Better Luck Next Time!")) / 2,
          SCREEN_SIZE / 2);
    }
    g.drawString("Highest Score: " + highestScore,
        (SCREEN_SIZE - metrics.stringWidth("Highest Score: " + highestScore)) / 2, SCREEN_SIZE / 2 + 40);

    // Display "New Game" option
    String newGameMessage = "Press N for New Game";
    g.drawString(newGameMessage, (SCREEN_SIZE - metrics.stringWidth(newGameMessage)) / 2, SCREEN_SIZE / 2 + 70);
  }

  private void move() {
    for (int i = snakeLength; i > 0; i--) {
      x[i] = x[i - 1];
      y[i] = y[i - 1];
    }
    switch (direction) {
      case 'L':
        x[0] -= TILE_SIZE;
        break;
      case 'R':
        x[0] += TILE_SIZE;
        break;
      case 'U':
        y[0] -= TILE_SIZE;
        break;
      case 'D':
        y[0] += TILE_SIZE;
        break;
    }
  }

  private void checkApple() {
    if (x[0] == appleX && y[0] == appleY) {
      snakeLength++;
      points += 10;
      spawnApple();
    } else if (bigApplePresent &&
        x[0] >= bigAppleX && x[0] < bigAppleX + TILE_SIZE * 2 &&
        y[0] >= bigAppleY && y[0] < bigAppleY + TILE_SIZE * 2) {
      snakeLength++;
      points += 20;
      bigApplePresent = false;
      spawnApple();
    }
  }

  private void checkCollision() {
    for (int i = snakeLength; i > 0; i--) {
      if (x[0] == x[i] && y[0] == y[i]) {
        running = false;
      }
    }
    if (x[0] < 0 || x[0] >= SCREEN_SIZE || y[0] < 0 || y[0] >= SCREEN_SIZE) {
      running = false;
    }
    if (!running) {
      updateHighestScore();
      timer.stop();
      repaint();
    }
  }

  private void updateHighestScore() {
    if (points > highestScore) {
      highestScore = points;
      saveHighestScore();
    }
  }

  private void loadHighestScore() {
    try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
      highestScore = Integer.parseInt(reader.readLine());
    } catch (IOException | NumberFormatException e) {
      highestScore = 0;
    }
  }

  private void saveHighestScore() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
      writer.write(String.valueOf(highestScore));
    } catch (IOException e) {
      System.err.println("Error saving highest score: " + e.getMessage());
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (running) {
      move();
      checkApple();
      checkCollision();
      repaint();
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Snake Game");
    SnakeGame game = new SnakeGame();
    frame.add(game);
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
