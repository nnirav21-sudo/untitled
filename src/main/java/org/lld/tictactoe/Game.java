package org.lld.tictactoe;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Scanner;

public class Game {
    Board playingBoard;
    Deque<Player> players;
    public Game() {
        initializeGame();
    }
    public void initializeGame() {
        players = new LinkedList<>();
        PlayingPeiceX peiceX = new PlayingPeiceX();
        Player player1 = new Player(peiceX,"player1");
        PlayingPeiceO peiceO = new PlayingPeiceO();
        Player player2 = new Player(peiceO,"player2");
        players.add(player1);
        players.add(player2);
        playingBoard = new Board(3);
    }
    public void startGame() {
        boolean gameOver = false;
        while (!gameOver) {
            Player currentPlayer = players.removeFirst();
            if(playingBoard.isEmpty()){
                gameOver = true;
                continue;
            }
            playingBoard.printBoard();
            System.out.println("Player:" + currentPlayer.name + "Enter row,column: ");
            Scanner inputScanner = new Scanner(System.in);
            String s = inputScanner.nextLine();
            String[] values = s.split(",");
            int row  = Integer.parseInt(values[0]);
            int column = Integer.parseInt(values[1]);
            boolean addedSuccessfully = playingBoard.addPeice(row,column,currentPlayer.playingPeice.peiceType);
            if (!addedSuccessfully) {
                System.out.println("Invalid row,column, try Again");
                players.addFirst(currentPlayer);
                continue;
            }
            if(isWinner(row,column,currentPlayer.playingPeice.peiceType)){
                System.out.println("Winner is player:"+currentPlayer.name);
                return;
            }
            players.addLast(currentPlayer);
        }
        System.out.println("Match is Tied");
    }

    private boolean isWinner(int row, int column, PeiceType peiceType) {
        boolean rowWinner = true;
        boolean columnWinner = true;

        // Check row
        for (int i = 0; i < playingBoard.size; i++) {
            if (playingBoard.board[row][i] == null || playingBoard.board[row][i] != peiceType) {
                rowWinner = false;
                break;
            }
        }

        // Check column
        for (int i = 0; i < playingBoard.size; i++) {
            if (playingBoard.board[i][column] == null || playingBoard.board[i][column] != peiceType) {
                columnWinner = false;
                break;
            }
        }

        // Check diagonals
        boolean leftDiagonalWinner = true;
        boolean rightDiagonalWinner = true;
        for (int i = 0; i < playingBoard.size; i++) {
            if (playingBoard.board[i][i] == null || playingBoard.board[i][i] != peiceType) {
                leftDiagonalWinner = false;
            }
            if (playingBoard.board[i][playingBoard.size - 1 - i] == null ||
                    playingBoard.board[i][playingBoard.size - 1 - i] != peiceType) {
                rightDiagonalWinner = false;
            }
        }

        return rowWinner || columnWinner || leftDiagonalWinner || rightDiagonalWinner;
    }


}
