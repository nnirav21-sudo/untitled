package nishant.lld.snakeLadder;

import java.util.*;

class Snake{
    int start;
    int end;
    Snake(int start, int end){
        this.start = start;
        this.end = end;
    }
}
class Ladder{
    int start;
    int end;
    Ladder(int start,int end){
        this.start = start;
        this.end = end;
    }
}
class Dice{
    private final int faces;
    Dice(int faces){
        this.faces = faces;
    }
    private final Random rand = new Random();
    int rollDice(){
        return 1 + rand.nextInt(faces);
    }
}

class Player{
    String id;
    String name;
    int pos;
    Player(String id,String name){
        this.id = id;
        this.name = name;
        this.pos=0;
    }
}
class Board{
    int size;
    Map<Integer,Snake> snakes = new HashMap<>();
    Map<Integer,Ladder> ladders = new HashMap<>();
    Board(int size,List<Snake> snakes,List<Ladder> ladders){
        this.size= size;
        setSnakes(snakes);
        setLadder(ladders);
    }
    void setSnakes(List<Snake> snakesList){
        for(Snake snake:snakesList){
            snakes.put(snake.start,snake);
        }
    }
    void setLadder(List<Ladder> laddersList){
        for( Ladder ladder:laddersList){
            ladders.put(ladder.start,ladder);
        }
    }

    int nextPos(int pos){
        if(snakes.containsKey(pos)){
            System.out.println("Bitten by snake at pos :" + pos);
            return snakes.get(pos).end;
        }
        if(ladders.containsKey(pos)){
            System.out.println("Climbed  ladder at pos :" + pos);
            return ladders.get(pos).end;
        }
        return pos;
    }

}
class Game{
    Dice dice;
    Deque<Player> players = new LinkedList<>();
    Board board;
    Game(Dice dice,Board board,List<Player> players){
        this.dice = dice;
        this.board = board;
        this.players= new LinkedList<>(players);
    }

    void startGame(){
        while(true){
            Player currentPlayer = players.poll();
            int currentRoll = dice.rollDice();
            int nextPos = currentPlayer.pos + currentRoll ;
            if(nextPos>board.size){
                System.out.println("turn skipped for current player");
                players.offer(currentPlayer);
                continue;
            }
            System.out.println("current Player :"+ currentPlayer.name + " rolled the dice :" + currentRoll);
            nextPos = board.nextPos(nextPos);
            if(nextPos == board.size){
                System.out.println("Player :" + currentPlayer.name + " Won the Game " );
                break;
            }
            currentPlayer.pos = nextPos;
            players.offer(currentPlayer);
        }
    }
}
public class SnakeLadderDemo {
    public static void main(String[] args) {
        Dice dice = new Dice(6);
        List<Snake> snakes = List.of( new Snake(17, 7), new Snake(54, 34),
                new Snake(62, 19), new Snake(98, 79));
        List<Ladder> ladders = List.of(new Ladder(3, 38), new Ladder(24, 33),
                new Ladder(42, 93), new Ladder(72, 84));
        Board board = new Board(100,snakes,ladders);
        List<Player> players = List.of( new Player("shu","shubham"),new Player("nis","nishant"));
        Game game = new Game(dice,board,players);
        game.startGame();
    }
}
