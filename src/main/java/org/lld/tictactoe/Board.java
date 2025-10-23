package org.lld.tictactoe;
class ABC{
    void  run(){
        System.out.println("abc");
    }
}
public class Board {
    int size;
    ABC abc = new ABC();
    PeiceType[][] board;
    public Board(int size) {
        this.size = size;
        // initialize a new board of Size size;
        board = new PeiceType[size][size];
        abc.run();
    }

    public boolean isEmpty(){
        boolean empty = true;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if(board[i][j]==null){
                    empty = false;
                }
            }
        }
       return empty;
    }
    public boolean addPeice(int i, int j,PeiceType peiceType){
        if(board[i][j]==null){
            board[i][j]=peiceType;
            return true;
        }
        return false ;
    }
    public void  printBoard(){
        for (int i = 0; i < size; i++) {
            System.out.print("| ");
            for (int j = 0; j < size; j++) {
                System.out.print(board[i][j]+" |");
            }
           System.out.println("\n");
        }
    }
}
