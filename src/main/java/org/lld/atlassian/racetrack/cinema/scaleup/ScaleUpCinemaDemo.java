package org.lld.atlassian.racetrack.cinema.scaleup;

import java.util.*;

class Movie {
    private final String title;
    private final int durationInMinutes;
    private final double revenue;

    public Movie(String title, int durationInMinutes, double revenue) {
        this.title = title;
        this.durationInMinutes = durationInMinutes;
        this.revenue = revenue;
    }

    public String getTitle() { return title; }
    public int getDurationInMinutes() { return durationInMinutes; }
    public double getRevenue() { return revenue; }
}

class Screening {
    private final Movie movie;
    private final int startTime;

    public Screening(Movie movie, int startTime) {
        this.movie = movie;
        this.startTime = startTime;
    }

    public Movie getMovie() { return movie; }
    public int getStartTime() { return startTime; }
    public int getEndTime() { return startTime + movie.getDurationInMinutes(); }

    @Override
    public String toString() {
        return movie.getTitle() + " (" + startTime + "-" + getEndTime() + ") $" + movie.getRevenue();
    }
}

class ScheduleValidator {
    private static final int OPEN_TIME = 600;   // 10:00
    private static final int CLOSE_TIME = 1380; // 23:00

    public boolean canAddScreening(List<Screening> screenings, Screening newScreening) {
        int newStart = newScreening.getStartTime();
        int newEnd = newScreening.getEndTime();

        if (newStart < OPEN_TIME || newEnd > CLOSE_TIME) return false;

        for (Screening s : screenings) {
            if (!(newEnd <= s.getStartTime() || newStart >= s.getEndTime())) {
                return false;
            }
        }
        return true;
    }
}

class Cinema {
    private final Map<String, Movie> movies = new HashMap<>();
    private final List<Screening> screenings = new ArrayList<>();
    private final ScheduleValidator validator = new ScheduleValidator();

    public void addMovie(String title, int duration, double revenue) {
        movies.put(title, new Movie(title, duration, revenue));
    }

    public void addScreening(String title, int startTime) {
        Movie movie = movies.get(title);
        if (movie == null) throw new IllegalArgumentException("Movie not found: " + title);
        screenings.add(new Screening(movie, startTime));
    }

    public boolean canAddNewScreening(String title, int startTime) {
        Movie movie = movies.get(title);
        if (movie == null) return false;
        Screening newScreening = new Screening(movie, startTime);
        return validator.canAddScreening(screenings, newScreening);
    }

    public List<Screening> getScreenings() {
        return new ArrayList<>(screenings);
    }

    public void printSchedule() {
        screenings.stream()
                .sorted(Comparator.comparingInt(Screening::getStartTime))
                .forEach(System.out::println);
    }
}

class RevenueScheduler {
    private static final int OPEN_TIME = 600;
    private static final int CLOSE_TIME = 1380;
    private final ScheduleValidator validator;

    public RevenueScheduler(ScheduleValidator validator) {
        this.validator = validator;
    }

    /**
     * Returns the screening to remove to fit newMovie for max revenue.
     * Returns null if newMovie fits without removing any screening.
     */
    public Screening planScreening(Movie newMovie, List<Screening> existingScreenings) {
        List<Screening> screenings = new ArrayList<>(existingScreenings);
        screenings.sort(Comparator.comparingInt(Screening::getStartTime));

        // 1️⃣ Try to fit without removing any screening
        for (int t = OPEN_TIME; t + newMovie.getDurationInMinutes() <= CLOSE_TIME; t += 10) {
            Screening candidate = new Screening(newMovie, t);
            if (validator.canAddScreening(screenings, candidate)) {
                return null; // fits without removing
            }
        }

        // 2️⃣ Schedule full → try removing one screening
        double bestRevenue = 0;
        Screening bestToRemove = null;

        for (Screening toRemove : screenings) {
            List<Screening> tempSchedule = new ArrayList<>(screenings);
            tempSchedule.remove(toRemove);

            // Try all possible start times in the day
            for (int t = OPEN_TIME; t + newMovie.getDurationInMinutes() <= CLOSE_TIME; t += 10) {
                Screening candidate = new Screening(newMovie, t);
                if (validator.canAddScreening(tempSchedule, candidate)) {
                    double totalRevenue = tempSchedule.stream().mapToDouble(s -> s.getMovie().getRevenue()).sum()
                            + newMovie.getRevenue();
                    if (totalRevenue > bestRevenue) {
                        bestRevenue = totalRevenue;
                        bestToRemove = toRemove;
                    }
                }
            }
        }

        return bestToRemove;
    }
}

public class ScaleUpCinemaDemo {
    public static void main(String[] args) {
        Cinema cinema = new Cinema();
        cinema.addMovie("Lord Of The Rings", 120, 600);
        cinema.addMovie("Back To The Future", 90, 500);
        cinema.addMovie("Inception", 100, 700); // new movie

        cinema.addScreening("Lord Of The Rings", 660);
        cinema.addScreening("Lord Of The Rings", 840);
        cinema.addScreening("Back To The Future", 1020);
        cinema.addScreening("Lord Of The Rings", 1200);

        System.out.println("Current Schedule:");
        cinema.printSchedule();

        Movie newMovie = new Movie("Inception", 100, 700);
        RevenueScheduler rs = new RevenueScheduler(new ScheduleValidator());

        Screening toRemove = rs.planScreening(newMovie, cinema.getScreenings());

        if (toRemove == null) {
            System.out.println("\n✅ '" + newMovie.getTitle() + "' can be added without removing any screening.");
        } else {
            System.out.println("\n❌ Schedule full. Remove this screening to fit new movie: " + toRemove);
        }
    }
}