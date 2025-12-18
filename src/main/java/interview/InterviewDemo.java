package interview;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InterviewDemo {

    public static void main(String[] args){
        TemaRepository repo = new TemaRepository();
        AdminServiceImpl admins = new AdminServiceImpl(repo,repo);

        var india = admins.addTeams("india");
        var aus = admins.addTeams("aus");

        admins.addPlayer(india.getId(),"Rohit");
        admins.addPlayer(india.getId(),"Kohli");
        admins.addPlayer(aus.getId(),"warner");
        admins.addPlayer(aus.getId(),"starc");

        Match match = admins.addMacth(
                india.getId(),aus.getId(),MatchType.ODI
        );

        admins.startMatch(match.getId());
        LiveScoreImpl  liveScore = new LiveScoreImpl(repo);




        liveScore.addBall(match.getId(),1,false,
                new Player("rohit","rphit"),new Player("Starc","starc"));
        liveScore.addBall(match.getId(),0,true,
                new Player("kohli","kohli"),new Player("Starc","starc"));
        liveScore.addBall(match.getId(),6,false,
                new Player("rohit","rphit"),new Player("warner","warner"));
        liveScore.addBall(match.getId(),1,false,
                new Player("rohit","rphit"),new Player("Starc","starc"));

        var innings = match.getInnings();
        System.out.println(
                "Score:" + innings.getRuns() + "/" + innings.getWickets()
        );



    }
}
// player -> team commentator admin match -> match Status , MatchType,

interface LiveScoreService {
    void addBall(String matchId, int runs, boolean wicket, Player baller, Player batter);
}

class LiveScoreImpl implements LiveScoreService{
   private final  MatchRepo matchRepo;

   public LiveScoreImpl(MatchRepo matchRepo){
       this.matchRepo = matchRepo;

   }

   public void addBall(String matchId , int runs , boolean wicket, Player baller , Player batter){

       Match match = matchRepo.findMatchById(matchId);
       match.getInnings().addBalls( new Ball( new BallEvent(runs,wicket,baller,batter)));

   }
}


class Player {
    private final String id;
    private final String name;

    public Player(String id, String name){
        this.id = id;
        this.name = name;
    }
}

@Data
class Team {
    private final String id;
    private final String name;
    private final List<Player> players;

    public Team(String id, String name ){
        this.id = id;
        this.name = name;
        this.players = new ArrayList<>();

    }
    public void addPlayer(Player player){
        // validations can be added later
        players.add(player);
    }

    // remove player method
}

enum MatchStatus{
    CREATED,STARTED,COMPLETED
}
enum MatchType{
    ODI,T20,TEST
}

@Data
class BallEvent{
    private final int run;
    private final boolean wicket;
    private final Player bowler;
    private final Player batter;
    public BallEvent(int run, boolean wicket,Player baller,Player batter){
        this.run = run;
        this.wicket =wicket;
        this.bowler = baller;
        this.batter = batter;

    }
}

@Data
class Ball{
    private final BallEvent event;
    // can add oher metadata

    public Ball(BallEvent event ){
        this.event = event;
    }
}

@Data
class Innings{
    private final Team battingTeam;

    private int runs;
    private final List<Ball> balls;
    private int wickets;

    public Innings(Team battingTeam){
        this.battingTeam = battingTeam;
        this.balls = new ArrayList<>();
    }

    public void addBalls(Ball ball){
        balls.add(ball);
        runs += ball.getEvent().getRun();
        if(ball.getEvent().isWicket()){
            wickets++;
        }

    }


}

@Data
class Match {
    private final String id;
    //private final String name;
    private final Team battingTeam;
    private final Team bowlingTeam;
    private final MatchType type;
    private MatchStatus status;
    private Innings innings; // later check LIST of innings or single inning ?

    public Match(String id, Team batTeam, Team bowlTeam, MatchType matchType){
        this.id = id;
        this.battingTeam = batTeam;
        this.bowlingTeam = bowlTeam;
        this.type = matchType;
        this.status = MatchStatus.CREATED;

    }

    public void start(){
        this.status = MatchStatus.STARTED;
        this.innings = new Innings(battingTeam);
    }

}

interface TeamRepo{
        Team save(Team team);
        Team findById(String id);
}

interface MatchRepo{
    Match save(Match match);
    Match findMatchById(String id);
}

class TemaRepository implements TeamRepo,MatchRepo {
    private final Map<String,Team> teams = new ConcurrentHashMap<>();
    private final Map<String,Match> matches = new ConcurrentHashMap<>();
    @Override
    public Match save(Match match) {
       matches.put(match.getId(),match);
       return match;
    }

    @Override
    public Match findMatchById(String id) {
        return matches.get(id);
    }

    @Override
    public Team save(Team team) {
       teams.put(team.getId(),team);
       return team;
    }

    @Override
    public Team findById(String id) {
        return teams.get(id);
    }

}

interface AdminService{
    Team addTeams(String name);
    Player addPlayer(String teamId, String playerName);
    Match addMacth(String teamId1,String teamId2,MatchType type);
    void  startMatch(String matchId);
}

 interface ScoreCardService{
    // Score card can be a class
    String getScore(String matchId);
}

class AdminServiceImpl implements AdminService{
    private final TeamRepo teamRepo;
    private final MatchRepo matchRepo;

    public AdminServiceImpl(TeamRepo teamRepo, MatchRepo matchRepo){
        this.teamRepo = teamRepo;
        this.matchRepo = matchRepo;
    }


    @Override
    public Team addTeams(String name) {
        Team team = new Team(UUID.randomUUID().toString(),name);
        return teamRepo.save(team);
    }

    @Override
    public Player addPlayer(String teamId, String playerName) {
        Team team = teamRepo.findById(teamId);
        // throw exception if(team == null) throw new NotFoundException("no team found");

        Player player = new Player(UUID.randomUUID().toString(),playerName);
        team.addPlayer(player);
        return player;
    }

    @Override
    public Match addMacth(String teamId1, String teamId2, MatchType type) {
       Team a = teamRepo.findById(teamId1);
       Team b = teamRepo.findById(teamId2);
       Match match = new Match(UUID.randomUUID().toString(),a,b,type);
       return matchRepo.save(match);
    }

    @Override
    public void startMatch(String matchId) {
        Match match  =  matchRepo.findMatchById(matchId);
        match.start();
    }
}










