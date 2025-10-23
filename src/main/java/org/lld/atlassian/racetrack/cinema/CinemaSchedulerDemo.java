package org.lld.atlassian.racetrack.cinema;

import java.util.*;

class Movie {
    private final String title;
    private final int durationInMinutes;

    public Movie(String title, int durationInMinutes) {
        this.title = title;
        this.durationInMinutes = durationInMinutes;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }
}

class Screening {
    private final Movie movie;
    private final int startTime; // in minutes since midnight

    public Screening(Movie movie, int startTime) {
        this.movie = movie;
        this.startTime = startTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return startTime + movie.getDurationInMinutes();
    }

    public Movie getMovie() {
        return movie;
    }

    @Override
    public String toString() {
        return movie.getTitle() + " (" + startTime + " - " + getEndTime() + ")";
    }
}

class ScheduleValidator {

    private static final int OPEN_TIME = 600;   // 10:00
    private static final int CLOSE_TIME = 1380; // 23:00

    public boolean canAddScreening(List<Screening> screenings, Screening newScreening) {
        int newStart = newScreening.getStartTime();
        int newEnd = newScreening.getEndTime();

        // Check cinema open-close constraints
        if (newStart < OPEN_TIME || newEnd > CLOSE_TIME) {
            return false;
        }

        // Sort existing screenings by start time
        List<Screening> sorted = new ArrayList<>(screenings);
        sorted.sort(Comparator.comparingInt(Screening::getStartTime));

        // Check overlaps
        for (Screening s : sorted) {
            int start = s.getStartTime();
            int end = s.getEndTime();

            boolean overlap = !(newEnd <= start || newStart >= end);
            if (overlap) {
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

    public void addMovie(String title, int duration) {
        movies.put(title, new Movie(title, duration));
    }

    public void addScreening(String title, int startTime) {
        Movie movie = movies.get(title);
        if (movie == null) {
            throw new IllegalArgumentException("Movie not found: " + title);
        }
        screenings.add(new Screening(movie, startTime));
    }

    public boolean canAddNewScreening(String title, int startTime) {
        Movie movie = movies.get(title);
        if (movie == null) return false;

        Screening newScreening = new Screening(movie, startTime);
        return validator.canAddScreening(screenings, newScreening);
    }

    public void printSchedule() {
        screenings.stream()
                .sorted(Comparator.comparingInt(Screening::getStartTime))
                .forEach(System.out::println);
    }
}

public class CinemaSchedulerDemo {
    public static void main(String[] args) {
        Cinema cinema = new Cinema();
        cinema.addMovie("Lord Of The Rings", 120);
        cinema.addMovie("Back To The Future", 90);

        cinema.addScreening("Lord Of The Rings", 660);
        cinema.addScreening("Lord Of The Rings", 840);
        cinema.addScreening("Back To The Future", 1020);
        cinema.addScreening("Lord Of The Rings", 1200);

        cinema.printSchedule();

        System.out.println("\nCan we add 'Back To The Future' at 1320?");
        boolean canAdd = cinema.canAddNewScreening("Back To The Future", 1320);
        System.out.println(canAdd ? "✅ Yes, fits in schedule" : "❌ No, overlaps or out of bounds");
    }
}