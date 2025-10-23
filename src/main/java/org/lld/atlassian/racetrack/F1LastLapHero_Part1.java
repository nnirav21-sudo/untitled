package org.lld.atlassian.racetrack;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

// ============================================================================
// Domain Models
// ============================================================================

/** Immutable record for a single lap. */
record Lap(double lapTime) {
    public Lap {
        if (lapTime <= 0) throw new IllegalArgumentException("Lap time must be positive");
    }
}

/** Immutable record for the Last Lap Hero result. */
record LastLapHeroResult(String driverName, double improvement)
        implements Comparable<LastLapHeroResult> {
    @Override
    public int compareTo(LastLapHeroResult other) {
        return Double.compare(this.improvement, other.improvement);
    }
    @Override
    public String toString() {
        return String.format("%s (improvement: %.2f)", driverName, improvement);
    }
}

/** Aggregates lap data for a driver (thread-safe). */
class DriverData {
    private final String name;
    private final List<Lap> laps = new ArrayList<>();
    public DriverData(String name) { this.name = Objects.requireNonNull(name); }

    public synchronized void addLap(Lap lap) { laps.add(lap); }
    public synchronized List<Lap> getLaps() { return List.copyOf(laps); }
    public String getName() { return name; }
}

// ============================================================================
// Strategy Pattern – Calculation
// ============================================================================

/** Strategy interface for calculating improvement. */
interface HeroCalculator {
    Optional<LastLapHeroResult> calculate(DriverData driverData);
    String getStrategyName();
}

/** Calculator for the base scenario (no pit stops). */
class BasicHeroCalculator implements HeroCalculator {
    @Override
    public Optional<LastLapHeroResult> calculate(DriverData driverData) {
        List<Lap> laps = driverData.getLaps();
        if (laps.isEmpty()) return Optional.empty();

        double avg = laps.stream().mapToDouble(Lap::lapTime).average().orElse(0);
        double last = laps.get(laps.size() - 1).lapTime();
        double improvement = avg - last;          // positive means faster than avg

        return Optional.of(new LastLapHeroResult(driverData.getName(), improvement));
    }

    @Override
    public String getStrategyName() { return "Basic (No Pit Stops)"; }
}

// ============================================================================
// Core Service – RaceManager
// ============================================================================

class RaceManager {
    private final Map<String, DriverData> drivers = new ConcurrentHashMap<>();

    public void addLap(String driverName, double lapTime) {
        drivers.computeIfAbsent(driverName, DriverData::new).addLap(new Lap(lapTime));
    }

    public Optional<LastLapHeroResult> getLastLapHero(HeroCalculator calc) {
        return drivers.values().stream()
                .map(calc::calculate).flatMap(Optional::stream)
                .max(Comparator.naturalOrder());       // higher improvement wins
    }

    public Map<String, Double> getAllImprovements(HeroCalculator calc) {
        return drivers.values().stream()
                .map(calc::calculate).flatMap(Optional::stream)
                .collect(Collectors.toMap(LastLapHeroResult::driverName,
                                          LastLapHeroResult::improvement));
    }
}

// ============================================================================
// Demo
// ============================================================================

public class F1LastLapHero_Part1 {
    public static void main(String[] args) {
        System.out.println("=== BASE PROBLEM – No Pit Stops ===\n");

        RaceManager rm = new RaceManager();
        rm.addLap("Driver1", 100);
        rm.addLap("Driver2", 90);
        rm.addLap("Driver3", 70);
        rm.addLap("Driver1", 110);
        rm.addLap("Driver2", 95);
        rm.addLap("Driver3", 50);

        HeroCalculator calc = new BasicHeroCalculator();
        var hero = rm.getLastLapHero(calc).orElseThrow();

        System.out.println("Improvements (avg − last):");
        rm.getAllImprovements(calc).forEach((d, imp) ->
            System.out.printf("  %s → %.2f%n", d, imp));

        System.out.printf("%nLast Lap Hero [%s] = %s%n",
                calc.getStrategyName(), hero);
    }
}
