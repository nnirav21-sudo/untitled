package org.lld.atlassian.tennis;

import java.util.*;

class BookingRecord {
    int id;
    int start;
    int end; // court becomes free at this time

    BookingRecord(int id, int start, int end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }
}

class Assignment {
    int bookingId;
    int courtId; // 0-based

    Assignment(int bookingId, int courtId) {
        this.bookingId = bookingId;
        this.courtId = courtId;
    }
}

public class CourtAssigner {

    private static class CourtFree {
        final int freeTime;
        final int courtId;
        CourtFree(int freeTime, int courtId) {
            this.freeTime = freeTime;
            this.courtId = courtId;
        }
    }

    public static List<Assignment> assignCourts(List<BookingRecord> bookings) {
        // Sort by start, then end, then id (deterministic)
        bookings.sort((a, b) -> {
            if (a.start != b.start) return Integer.compare(a.start, b.start);
            if (a.end != b.end)     return Integer.compare(a.end, b.end);
            return Integer.compare(a.id, b.id);
        });

        // (freeTime, courtId) ordered; tie-break by courtId to keep comparator strict
        TreeSet<CourtFree> available = new TreeSet<>((x, y) -> {
            if (x.freeTime != y.freeTime) return Integer.compare(x.freeTime, y.freeTime);
            return Integer.compare(x.courtId, y.courtId);
        });

        List<Assignment> plan = new ArrayList<>();
        int nextCourtId = 0;

        for (BookingRecord bk : bookings) {
            CourtFree reuse = available.isEmpty() ? null : available.first();

            if (reuse != null && reuse.freeTime <= bk.start) {
                // reuse this court
                available.remove(reuse);
                plan.add(new Assignment(bk.id, reuse.courtId));
                available.add(new CourtFree(bk.end, reuse.courtId));
            } else {
                // need a new court
                int court = nextCourtId++;
                plan.add(new Assignment(bk.id, court));
                available.add(new CourtFree(bk.end, court));
            }
        }
        return plan;
    }

    // quick demo
    public static void main(String[] args) {
        List<BookingRecord> bookings = Arrays.asList(
            new BookingRecord(101, 0, 5),
            new BookingRecord(102, 1, 4),
            new BookingRecord(103, 3, 8),
            new BookingRecord(104, 5, 7),
            new BookingRecord(105, 8, 10),
            new BookingRecord(106, 10, 12)
        );

        List<Assignment> plan = assignCourts(bookings);

        int maxCourt = -1;
        for (Assignment a : plan) {
            maxCourt = Math.max(maxCourt, a.courtId);
            System.out.println("Booking " + a.bookingId + " -> Court " + a.courtId);
        }
        System.out.println("Minimum courts needed: " + (maxCourt + 1));
    }
}
