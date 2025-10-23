package org.lld.atlassian.racetrack.shubham;

import java.util.*;

class Agent{
    String name;
    Agent(String name){this.name=name;}
}

class Rating{
    Agent agent;
    int score;

    Rating(Agent agent, int score){
        this.agent = agent;
        this.score = score;
    }
}

class AgentRatings{
    List<Rating> ratings;

    AgentRatings(){
        this.ratings = new ArrayList<>();
    }

    void addRating(Rating rating){
        ratings.add(rating);
    }

    double getAverage(){
        double sum = 0.0;
        for(Rating rating: this.ratings){
            sum += rating.score;
        }
        return sum/(this.ratings.size());
    }
}

public class RatingCalculator {

    Map<String, Agent> agentMap = new HashMap<>();
    Map<Agent, AgentRatings> agentRatingsMap = new HashMap<>();

    void addRating(String agentName, int score){
        if(score<1||score>5) throw new RuntimeException("Allowed rates is 1-5");
        Agent agent = agentMap.get(agentName);
        AgentRatings agentRatings = agentRatingsMap.get(agent);
        agentRatings.addRating(new Rating(agent, score));
    }

    void addAgent(String name){
        if(!agentMap.containsKey(name)){
            Agent agent = new Agent(name);
            agentMap.put(name, agent);
            agentRatingsMap.put(agent, new AgentRatings());
        }
    }

    void displayAverage(){
        agentRatingsMap.entrySet().stream()
                .sorted(
                        Comparator.<Map.Entry<Agent, AgentRatings>>comparingDouble(
                                        e -> e.getValue().getAverage())
                                .reversed()
                )
                .forEach(e -> System.out.printf("%s : %.2f%n",
                        e.getKey().name, e.getValue().getAverage()));

    }

    static void main(String[] args) {

        RatingCalculator ratingCalculator = new RatingCalculator();
        ratingCalculator.addAgent("Shubham");
        ratingCalculator.addAgent("Nishant");
        ratingCalculator.addAgent("Lucky");

        ratingCalculator.addRating("Shubham", 4);
        ratingCalculator.addRating("Nishant", 5);
        ratingCalculator.addRating("Lucky", 4);
        ratingCalculator.addRating("Shubham", 3);

        ratingCalculator.displayAverage();

    }
}