package org.lld.atlassian.racetrack;

import java.util.*;

class Movie {
    private final String title;
    private final int durationInMinutes;
    private final int revenue;

    public Movie(String title, int durationInMinutes, int revenue) {
        this.title = title;
        this.durationInMinutes = durationInMinutes;
        this.revenue = revenue;
    }

    public String getTitle() { return title; }
    public int getDurationInMinutes() { return durationInMinutes; }
    public int getRevenue() { return revenue; }
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
        return String.format("%s (%d–%d)", movie.getTitle(), startTime, getEndTime());
    }
}

class MovieSchedule {
    private final List<Screening> screenings;

    public MovieSchedule(List<Screening> screenings) {
        screenings.sort(Comparator.comparingInt(Screening::getStartTime));
        this.screenings = screenings;
    }

    public List<Screening> getScreenings() { return screenings; }

    public int totalRevenue() {
        return screenings.stream().mapToInt(s -> s.getMovie().getRevenue()).sum();
    }

    public boolean canAdd(Movie movie, int startTime) {
        int endTime = startTime + movie.getDurationInMinutes();
        if (startTime < 600 || endTime > 1380) return false; // 10:00–23:00

        for (Screening s : screenings) {
            if (Math.max(s.getStartTime(), startTime) < Math.min(s.getEndTime(), endTime))
                return false; // overlap
        }
        return true;
    }
}

class CinemaPlanner {

    public boolean canAddMovie(Movie newMovie, MovieSchedule schedule) {
        for (int time = 600; time + newMovie.getDurationInMinutes() <= 1380; time += 5)
            if (schedule.canAdd(newMovie, time))
                return true;
        return false;
    }

    public int findBestInsertionTime(Movie newMovie, MovieSchedule schedule) {
        for (int time = 600; time + newMovie.getDurationInMinutes() <= 1380; time += 5)
            if (schedule.canAdd(newMovie, time))
                return time;
        return -1;
    }

    public void planScreening(Movie newMovie, MovieSchedule schedule) {
        int currentRevenue = schedule.totalRevenue();

        // ✅ Step 1: Try to schedule without removing anything
        int bestTime = findBestInsertionTime(newMovie, schedule);
        if (bestTime != -1) {
            System.out.println("✅ Can schedule '" + newMovie.getTitle() + "' at " + bestTime);
            System.out.println("Total revenue stays $" + (currentRevenue + newMovie.getRevenue()));
            return;
        }

        // 🚫 Step 2: Otherwise, try removing one screening to maximize earnings
        int maxRevenue = currentRevenue;
        Screening toRemove = null;

        for (Screening s : schedule.getScreenings()) {
            List<Screening> modified = new ArrayList<>(schedule.getScreenings());
            modified.remove(s);
            MovieSchedule newSchedule = new MovieSchedule(modified);

            int insertTime = findBestInsertionTime(newMovie, newSchedule);
            if (insertTime != -1) {
                int newRevenue = currentRevenue - s.getMovie().getRevenue() + newMovie.getRevenue();
                if (newRevenue > maxRevenue) {
                    maxRevenue = newRevenue;
                    toRemove = s;
                }
            }
        }

        if (toRemove != null) {
            System.out.println("🔁 Replace: " + toRemove + " → add '" + newMovie.getTitle() + "'");
            System.out.println("💰 Revenue increases by $" + (maxRevenue - currentRevenue));
        } else {
            System.out.println("❌ Cannot fit '" + newMovie.getTitle() + "' even after removing one screening.");
        }
    }
}

public class CinemaSchedulerApp {
    public static void main(String[] args) {
        Movie lotr = new Movie("Lord Of The Rings", 120, 600);
        Movie bttf = new Movie("Back To The Future", 90, 500);
        Movie avengers = new Movie("Avengers", 150, 800);

        List<Screening> list = Arrays.asList(
                new Screening(lotr, 660),
                new Screening(lotr, 840),
                new Screening(bttf, 1020),
                new Screening(lotr, 1200)
        );

        MovieSchedule schedule = new MovieSchedule(list);
        CinemaPlanner planner = new CinemaPlanner();

        System.out.println("🎟 Current Total Revenue: $" + schedule.totalRevenue());
        planner.planScreening(avengers, schedule);
    }
}
