package org.lld.atlassian.racetrack.cinema;

import java.util.HashMap;
import java.util.Map;

class Agent {
    private final String name;

    Agent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

class Rating {
    private final Agent agent;
    private final int score;

    Rating(Agent agent, int score) {
        this.agent = agent;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public Agent getAgent() {
        return agent;
    }
}

class AgentRatings {
    private int totalRatingSum;
    private int ratingCount;
    private int fiveStarCount;
    private int aboveThreeCount;

    AgentRatings() {
        this.totalRatingSum = 0;
        this.ratingCount = 0;
        this.fiveStarCount = 0;
        this.aboveThreeCount = 0;
    }

    void addRating(Rating rating) {
        int score = rating.getScore();
        this.totalRatingSum += score;
        this.ratingCount++;

        if (score == 5) fiveStarCount++;
        if (score > 3) aboveThreeCount++;
    }

    double getAverage() {
        return ratingCount == 0 ? 0.0 : (double) totalRatingSum / ratingCount;
    }

    int getTotalRatings() {
        return ratingCount;
    }

    int getFiveStarCount() {
        return fiveStarCount;
    }

    int getAboveThreeCount() {
        return aboveThreeCount;
    }
}

public class RatingCalculator {
    private final Map<String, Agent> agentMap = new HashMap<>();
    private final Map<Agent, AgentRatings> agentRatingsMap = new HashMap<>();

    void addAgent(String name) {
        if (!agentMap.containsKey(name)) {
            Agent agent = new Agent(name);
            agentMap.put(name, agent);
            agentRatingsMap.put(agent, new AgentRatings());
        }
    }

    void addRating(String agentName, int score) {
        if (score < 1 || score > 5)
            throw new IllegalArgumentException("Rating must be between 1 and 5");

        Agent agent = agentMap.get(agentName);
        if (agent == null)
            throw new IllegalArgumentException("Agent not found: " + agentName);

        Rating rating = new Rating(agent, score);
        agentRatingsMap.get(agent).addRating(rating);
    }

    void displayAll() {
        System.out.println("\n=== AGENT PERFORMANCE REPORT ===");
        agentRatingsMap.forEach((agent, ratings) -> {
            System.out.printf("%-10s | Avg: %.2f | Total: %d | 5★: %d | >3★: %d%n",
                    agent.getName(),
                    ratings.getAverage(),
                    ratings.getTotalRatings(),
                    ratings.getFiveStarCount(),
                    ratings.getAboveThreeCount());
        });
    }

    public static void main(String[] args) {
        RatingCalculator rc = new RatingCalculator();

        rc.addAgent("Shubham");
        rc.addAgent("Nishant");
        rc.addAgent("Lucky");
        rc.addAgent("Deepak");

        rc.addRating("Shubham", 5);
        rc.addRating("Shubham", 3);
        rc.addRating("Nishant", 4);
        rc.addRating("Lucky", 5);
        rc.addRating("Deepak", 5);
        rc.addRating("Deepak", 4);

        rc.displayAll();
    }
}
