package org.lld.atlassian.racetrack;

import java.time.LocalDate;
import java.util.*;

// ===========================================
// Domain Classes
// ===========================================
class Agent {
    String name;
    Agent(String name) { this.name = name; }
}

class Rating {
    Agent agent;
    int score;
    LocalDate date;

    Rating(Agent agent, int score) {
        this(agent, score, LocalDate.now()); // default = today
    }

    Rating(Agent agent, int score, LocalDate date) {
        this.agent = agent;
        this.score = score;
        this.date = date;
    }
}

class AgentRatings {
    List<Rating> ratings;

    AgentRatings() {
        this.ratings = new ArrayList<>();
    }

    void addRating(Rating rating) {
        ratings.add(rating);
    }

    // ---- Overall Average ----
    double getAverage() {
        if (ratings.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Rating r : ratings) sum += r.score;
        return sum / ratings.size();
    }

    // ---- Monthly Average ----
    double getMonthlyAverage(int year, int month) {
        double sum = 0.0;
        int count = 0;
        for (Rating r : ratings) {
            if (r.date.getYear() == year && r.date.getMonthValue() == month) {
                sum += r.score;
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    // ---- 5★ Counts ----
    int getCountOfFiveStars() {
        int count = 0;
        for (Rating r : ratings) if (r.score == 5) count++;
        return count;
    }

    int getCountOfFiveStars(int year, int month) {
        int count = 0;
        for (Rating r : ratings) {
            if (r.score == 5 &&
                r.date.getYear() == year &&
                r.date.getMonthValue() == month) {
                count++;
            }
        }
        return count;
    }

    // ---- >3★ Counts ----
    int getCountAboveThree() {
        int count = 0;
        for (Rating r : ratings) if (r.score > 3) count++;
        return count;
    }

    int getCountAboveThree(int year, int month) {
        int count = 0;
        for (Rating r : ratings) {
            if (r.score > 3 &&
                r.date.getYear() == year &&
                r.date.getMonthValue() == month) {
                count++;
            }
        }
        return count;
    }

    // ---- Total Counts ----
    int getTotalRatings() {
        return ratings.size();
    }

    int getMonthlyRatingsCount(int year, int month) {
        int count = 0;
        for (Rating r : ratings) {
            if (r.date.getYear() == year && r.date.getMonthValue() == month) {
                count++;
            }
        }
        return count;
    }
}

// ===========================================
// Strategy Pattern for Sorting
// ===========================================
interface RatingComparatorStrategy extends Comparator<Map.Entry<Agent, AgentRatings>> {}

// Default Strategy: Compare only by average rating
class DefaultRatingComparator implements RatingComparatorStrategy {
    @Override
    public int compare(Map.Entry<Agent, AgentRatings> e1, Map.Entry<Agent, AgentRatings> e2) {
        return Double.compare(e2.getValue().getAverage(), e1.getValue().getAverage());
    }
}

// Alternate Strategy: avg → total ratings count
class TotalRatingsPriorityComparator implements RatingComparatorStrategy {
    @Override
    public int compare(Map.Entry<Agent, AgentRatings> e1, Map.Entry<Agent, AgentRatings> e2) {
        AgentRatings r1 = e1.getValue();
        AgentRatings r2 = e2.getValue();
        int cmp = Double.compare(r2.getAverage(), r1.getAverage());
        if (cmp != 0) return cmp;
        return Integer.compare(r2.getTotalRatings(), r1.getTotalRatings());
    }
}

// ===========================================
// Main Rating Calculator
// ===========================================
public class RatingCalculator {

    Map<String, Agent> agentMap = new HashMap<>();
    Map<Agent, AgentRatings> agentRatingsMap = new HashMap<>();

    void addAgent(String name) {
        if (!agentMap.containsKey(name)) {
            Agent agent = new Agent(name);
            agentMap.put(name, agent);
            agentRatingsMap.put(agent, new AgentRatings());
        }
    }

    void addRating(String agentName, int score) {
        addRating(agentName, score, LocalDate.now());
    }

    void addRating(String agentName, int score, LocalDate date) {
        if (score < 1 || score > 5)
            throw new RuntimeException("Allowed ratings are 1–5");
        Agent agent = agentMap.get(agentName);
        if (agent == null)
            throw new RuntimeException("Agent not found: " + agentName);
        agentRatingsMap.get(agent).addRating(new Rating(agent, score, date));
    }

    // ---- Display Overall Averages ----
    void displayAverage(RatingComparatorStrategy strategy) {
        System.out.println("=== Overall Average Ratings ===");
        agentRatingsMap.entrySet().stream()
                .sorted(strategy)
                .forEach(e -> {
                    AgentRatings ar = e.getValue();
                    System.out.printf(
                            "%s : Avg = %.2f | 5★ = %d | >3★ = %d | Total = %d%n",
                            e.getKey().name,
                            ar.getAverage(),
                            ar.getCountOfFiveStars(),
                            ar.getCountAboveThree(),
                            ar.getTotalRatings()
                    );
                });
    }

    // ---- Display Monthly Averages ----
    void displayMonthlyAverage(int year, int month, RatingComparatorStrategy strategy) {
        System.out.printf("%n=== Monthly Average Ratings for %d-%02d ===%n", year, month);
        agentRatingsMap.entrySet().stream()
                .sorted(strategy)
                .forEach(e -> {
                    AgentRatings ar = e.getValue();
                    System.out.printf(
                            "%s : Monthly Avg = %.2f | 5★ = %d | >3★ = %d | Total Ratings = %d%n",
                            e.getKey().name,
                            ar.getMonthlyAverage(year, month),
                            ar.getCountOfFiveStars(year, month),
                            ar.getCountAboveThree(year, month),
                            ar.getMonthlyRatingsCount(year, month)
                    );
                });
    }

    // =======================================
    // Main Demo
    // =======================================
    public static void main(String[] args) {
        RatingCalculator rc = new RatingCalculator();

        rc.addAgent("Shubham");
        rc.addAgent("Nishant");
        rc.addAgent("Lucky");
        rc.addAgent("Deepak");

        // October ratings
        rc.addRating("Shubham", 5, LocalDate.of(2025, 10, 5));
        rc.addRating("Nishant", 4, LocalDate.of(2025, 10, 6));
        rc.addRating("Deepak", 5, LocalDate.of(2025, 10, 7));

        // September ratings
        rc.addRating("Shubham", 3, LocalDate.of(2025, 9, 15));
        rc.addRating("Lucky", 5, LocalDate.of(2025, 9, 12));
        rc.addRating("Deepak", 4, LocalDate.of(2025, 9, 10));

        // Display
        rc.displayAverage(new DefaultRatingComparator()); // overall
        rc.displayMonthlyAverage(2025, 10, new DefaultRatingComparator()); // October
        rc.displayMonthlyAverage(2025, 9, new DefaultRatingComparator());  // September
    }
}
