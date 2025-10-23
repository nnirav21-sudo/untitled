package org.lld.atlassian.snake.scaleup;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

enum Direction { UP, DOWN, LEFT, RIGHT }

final class Position {
    private final int x, y;
    private Position(int x, int y) { this.x = x; this.y = y; }
    public static Position of(int x, int y) { return new Position(x, y); }

    public Position next(Direction dir) {
        return switch (dir) {
            case UP -> new Position(x - 1, y);
            case DOWN -> new Position(x + 1, y);
            case LEFT -> new Position(x, y - 1);
            case RIGHT -> new Position(x, y + 1);
        };
    }

    public int x() { return x; }
    public int y() { return y; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position p)) return false;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() { return Objects.hash(x, y); }

    @Override
    public String toString() { return "(" + x + "," + y + ")"; }
}

interface SnakeGame {
    void setDirection(Direction direction);
    boolean isGameOver();
    List<Position> getSnakeBody();
    void stopGame();
}

class Snake {
    private final Deque<Position> body = new ArrayDeque<>();

    public Snake(Position start, int initialSize) {
        // ✅ FIXED: reverse order so head is last (rightmost)
        for (int i = initialSize - 1; i >= 0; i--) {
            body.addLast(Position.of(start.x(), start.y() - i));
        }
    }

    public boolean contains(Position pos) { return body.contains(pos); }
    public Position head() { return body.peekLast(); }

    public void move(Position newHead, boolean grow) {
        body.addLast(newHead);
        if (!grow) body.removeFirst();
    }

    public List<Position> getBody() { return List.copyOf(body); }
}

class SnakeGameEngine implements SnakeGame {
    private static final int INITIAL_SIZE = 3;
    private static final int GROWTH_RATE = 5;

    private final int rows, cols;
    private final Snake snake;
    private volatile Direction currentDirection = Direction.RIGHT;
    private final AtomicBoolean gameOver = new AtomicBoolean(false);
    private int moveCount = 0;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SnakeGameEngine(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        // ✅ FIXED: start in middle, not top edge
        this.snake = new Snake(Position.of(rows / 2, cols / 2), INITIAL_SIZE);
        startAutoMove();
    }

    private void startAutoMove() {
        scheduler.scheduleAtFixedRate(this::moveOnce, 1, 1, TimeUnit.SECONDS);
    }

    private void moveOnce() {
        if (gameOver.get()) return;

        Position next = snake.head().next(currentDirection);
        if (isOutOfBounds(next) || snake.contains(next)) {
            gameOver.set(true);
            System.out.println("💥 Game Over! Final Snake: " + snake.getBody());
            stopGame();
            return;
        }

        moveCount++;
        boolean grow = moveCount % GROWTH_RATE == 0;
        snake.move(next, grow);
        System.out.println("Move #" + moveCount + " | Direction: " + currentDirection + " | Snake: " + snake.getBody());
    }

    private boolean isOutOfBounds(Position p) {
        return p.x() < 0 || p.x() >= rows || p.y() < 0 || p.y() >= cols;
    }

    @Override
    public void setDirection(Direction direction) {
        if (isOpposite(direction)) return; // Prevent 180-degree turns
        this.currentDirection = direction;
    }

    private boolean isOpposite(Direction dir) {
        return (currentDirection == Direction.UP && dir == Direction.DOWN) ||
                (currentDirection == Direction.DOWN && dir == Direction.UP) ||
                (currentDirection == Direction.LEFT && dir == Direction.RIGHT) ||
                (currentDirection == Direction.RIGHT && dir == Direction.LEFT);
    }

    @Override
    public boolean isGameOver() { return gameOver.get(); }

    @Override
    public List<Position> getSnakeBody() { return snake.getBody(); }

    @Override
    public void stopGame() {
        scheduler.shutdownNow();
    }
}

public class SnakeGameApp {
    public static void main(String[] args) throws InterruptedException {
        SnakeGameEngine game = new SnakeGameEngine(10, 10); // ✅ smaller grid for quick testing

        Thread.sleep(2500);
        game.setDirection(Direction.DOWN);

        Thread.sleep(3000);
        game.setDirection(Direction.RIGHT);

        Thread.sleep(2000);
        game.setDirection(Direction.UP);

        while (!game.isGameOver()) {
            Thread.sleep(1000);
        }
    }
}
