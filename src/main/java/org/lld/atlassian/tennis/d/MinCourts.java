package org.lld.atlassian.tennis.d;

import java.util.*;

class BookingRecord {
    int start;
    int end; // court is free at 'end'
    BookingRecord(int start, int end) {
        this.start = start;
        this.end = end;
    }
}

public class MinCourts {

    // O(n log n) time, O(n) space
    public static int minCourtsNeeded(List<BookingRecord> bookings) {
        if (bookings == null || bookings.isEmpty()) return 0;

        // +1 at start, -1 at end (end treated as exclusive)
        TreeMap<Integer, Integer> delta = new TreeMap<>();
        for (BookingRecord b : bookings) {
            delta.put(b.start, delta.getOrDefault(b.start, 0) + 1);
            delta.put(b.end,   delta.getOrDefault(b.end,   0) - 1);
        }

        int curr = 0, ans = 0;
        for (int d : delta.values()) {
            curr += d;
            ans = Math.max(ans, curr);
        }
        return ans;
    }

    // quick demo
    public static void main(String[] args) {
        List<BookingRecord> bookings = Arrays.asList(
            new BookingRecord(0, 5),
            new BookingRecord(1, 4),
            new BookingRecord(3, 8),
            new BookingRecord(5, 7),
            new BookingRecord(8,10),
            new BookingRecord(10,12)
        );
        System.out.println(minCourtsNeeded(bookings)); // prints 3
    }
}
