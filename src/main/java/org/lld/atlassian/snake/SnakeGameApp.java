package org.lld.atlassian.snake;

import java.util.*;
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
    void moveSnake(Direction direction);
    boolean isGameOver();
    List<Position> getSnakeBody();
}

class Snake {
    private final Deque<Position> body = new ArrayDeque<>();

    public Snake(Position start, int initialSize) {
        // ✅ FIXED: reverse loop so head is at the last element
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
    private Direction currentDirection = Direction.RIGHT;
    private final AtomicBoolean gameOver = new AtomicBoolean(false);
    private int moveCount = 0;

    public SnakeGameEngine(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        // ✅ FIXED: center start position so not at boundary
        this.snake = new Snake(Position.of(rows / 2, cols / 2), INITIAL_SIZE);
    }

    @Override
    public void moveSnake(Direction direction) {
        if (gameOver.get()) return;
        currentDirection = direction;
        moveOnce();
    }

    private void moveOnce() {
        Position next = snake.head().next(currentDirection);
        if (isOutOfBounds(next) || snake.contains(next)) {
            gameOver.set(true);
            return;
        }

        moveCount++;
        boolean grow = moveCount % GROWTH_RATE == 0;
        snake.move(next, grow);
    }

    private boolean isOutOfBounds(Position p) {
        return p.x() < 0 || p.x() >= rows || p.y() < 0 || p.y() >= cols;
    }

    @Override
    public boolean isGameOver() { return gameOver.get(); }

    @Override
    public List<Position> getSnakeBody() { return snake.getBody(); }

    public void printBoard() {
        System.out.println("Snake: " + getSnakeBody());
        System.out.println("Moves: " + moveCount + " | Game Over: " + isGameOver());
    }
}

public class SnakeGameApp {
    public static void main(String[] args) {
        SnakeGameEngine game = new SnakeGameEngine(10, 10);

        game.moveSnake(Direction.RIGHT);
        game.printBoard();
        game.moveSnake(Direction.RIGHT);
        game.printBoard();
        game.moveSnake(Direction.DOWN);
        game.printBoard();
        game.moveSnake(Direction.DOWN);
        game.printBoard();
        game.moveSnake(Direction.LEFT);
        game.printBoard();
        game.moveSnake(Direction.UP);
        game.printBoard();
    }
}
