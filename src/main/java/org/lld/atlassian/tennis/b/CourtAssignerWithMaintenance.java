package org.lld.atlassian.tennis.b;

import java.util.*;

class BookingRecord {
    int id;
    int start;
    int end; // court is done with the booking at this time

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

public class CourtAssignerWithMaintenance {

    private static class CourtFree {
        final int freeTime;
        final int courtId;
        CourtFree(int freeTime, int courtId) {
            this.freeTime = freeTime;
            this.courtId = courtId;
        }
    }

    /**
     * Assign bookings to courts with a fixed maintenance time X after each booking.
     * Uses the minimum number of courts; returns booking -> court plan.
     */
    public static List<Assignment> assignCourtsWithMaintenance(List<BookingRecord> bookings, int maintenanceTime) {
        if (bookings == null) return Collections.emptyList();

        // Sort bookings by start, then end, then id (deterministic)
        bookings.sort((a, b) -> {
            if (a.start != b.start) return Integer.compare(a.start, b.start);
            if (a.end != b.end)     return Integer.compare(a.end, b.end);
            return Integer.compare(a.id, b.id);
        });

        // (freeTime, courtId), ordered; tie-break on courtId to keep comparator strict
        TreeSet<CourtFree> available = new TreeSet<>((x, y) -> {
            if (x.freeTime != y.freeTime) return Integer.compare(x.freeTime, y.freeTime);
            return Integer.compare(x.courtId, y.courtId);
        });

        List<Assignment> plan = new ArrayList<>();
        int nextCourtId = 0;

        for (BookingRecord bk : bookings) {
            CourtFree reuse = available.isEmpty() ? null : available.first();

            // A court can be reused if it's free on or before the booking's start
            if (reuse != null && reuse.freeTime <= bk.start) {
                available.remove(reuse);
                plan.add(new Assignment(bk.id, reuse.courtId));
                // court becomes free after end + maintenance buffer
                available.add(new CourtFree(bk.end + maintenanceTime, reuse.courtId));
            } else {
                int court = nextCourtId++;
                plan.add(new Assignment(bk.id, court));
                available.add(new CourtFree(bk.end + maintenanceTime, court));
            }
        }
        return plan;
    }

    // --- quick demo ---
    public static void main(String[] args) {
        List<BookingRecord> bookings = Arrays.asList(
            new BookingRecord(101, 0, 5),
            new BookingRecord(102, 1, 4),
            new BookingRecord(103, 3, 8),
            new BookingRecord(104, 5, 7),
            new BookingRecord(105, 8, 10),
            new BookingRecord(106, 10, 12)
        );

        int X = 2; // maintenance time after every booking
        List<Assignment> plan = assignCourtsWithMaintenance(bookings, X);

        int maxCourt = -1;
        for (Assignment a : plan) {
            maxCourt = Math.max(maxCourt, a.courtId);
            System.out.println("Booking " + a.bookingId + " -> Court " + a.courtId);
        }
        System.out.println("Minimum courts needed (with X=" + X + "): " + (maxCourt + 1));
    }
}
