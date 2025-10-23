package org.lld.atlassian.tennis.c;

import java.util.*;

class BookingRecord {
    int id;
    int start;
    int end; // court work ends at this time

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

public class CourtAssignerDurable {

    private static class CourtNode {
        int freeTime;              // when this court is next available
        final int courtId;
        int usesSinceMaintenance;  // bookings served since last maintenance

        CourtNode(int freeTime, int courtId, int uses) {
            this.freeTime = freeTime;
            this.courtId = courtId;
            this.usesSinceMaintenance = uses;
        }
    }

    /**
     * Assign bookings to courts with per-court durability and maintenance time.
     * After each court serves 'durability' bookings, it incurs a maintenance delay (maintenanceTime).
     * Returns a booking -> court plan that uses the minimum number of courts.
     *
     * @param bookings list of bookings
     * @param maintenanceTime Y - maintenance duration after each X uses
     * @param durability X - number of bookings a court can serve before maintenance
     */
    public static List<Assignment> assignCourtsWithDurability(List<BookingRecord> bookings,
                                                              int maintenanceTime,
                                                              int durability) {
        if (bookings == null || bookings.isEmpty()) return Collections.emptyList();
        if (durability <= 0) throw new IllegalArgumentException("durability must be >= 1");
        if (maintenanceTime < 0) throw new IllegalArgumentException("maintenanceTime must be >= 0");

        // Sort by start, then end, then id for determinism
        bookings.sort((a, b) -> {
            if (a.start != b.start) return Integer.compare(a.start, b.start);
            if (a.end != b.end)     return Integer.compare(a.end, b.end);
            return Integer.compare(a.id, b.id);
        });

        // Courts ordered by earliest free time; tie-break by courtId for strict ordering
        TreeSet<CourtNode> byFreeTime = new TreeSet<>((x, y) -> {
            if (x.freeTime != y.freeTime) return Integer.compare(x.freeTime, y.freeTime);
            return Integer.compare(x.courtId, y.courtId);
        });

        List<Assignment> plan = new ArrayList<>();
        int nextCourtId = 0;

        for (BookingRecord bk : bookings) {
            CourtNode chosen = (!byFreeTime.isEmpty() && byFreeTime.first().freeTime <= bk.start)
                    ? byFreeTime.first()
                    : null;

            if (chosen != null) {
                byFreeTime.remove(chosen); // remove before mutating
                plan.add(new Assignment(bk.id, chosen.courtId));

                // Update usage and freeTime (inject maintenance after hitting durability)
                chosen.usesSinceMaintenance += 1;
                if (chosen.usesSinceMaintenance >= durability) {
                    chosen.freeTime = bk.end + maintenanceTime; // maintenance kicks in
                    chosen.usesSinceMaintenance = 0;             // reset after maintenance
                } else {
                    chosen.freeTime = bk.end; // no maintenance yet
                }

                byFreeTime.add(chosen);
            } else {
                // Need a new court
                int court = nextCourtId++;
                plan.add(new Assignment(bk.id, court));

                int uses = 1;
                int free = (uses >= durability) ? bk.end + maintenanceTime : bk.end;
                if (uses >= durability) uses = 0; // reset after immediate maintenance

                byFreeTime.add(new CourtNode(free, court, uses));
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

        int durability = 2;     // X bookings per maintenance
        int maintenanceTime = 3; // Y time units of maintenance

        List<Assignment> plan = assignCourtsWithDurability(bookings, maintenanceTime, durability);

        int maxCourt = -1;
        for (Assignment a : plan) {
            maxCourt = Math.max(maxCourt, a.courtId);
            System.out.println("Booking " + a.bookingId + " -> Court " + a.courtId);
        }
        System.out.println("Minimum courts used: " + (maxCourt + 1));
    }
}
