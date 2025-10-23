package org.lld.parkinglot;

import org.lld.parkinglot.parkingslot.ParkingSpot;

import java.util.List;
import java.util.Map;

class DisplayBoard {
    private int id;
    private Map<String, List<ParkingSpot>> parkingSpots;

    public DisplayBoard(int id) { /* ... */ }
    public void addParkingSpot(String spotType, List<ParkingSpot> spots) { /* ... */ }
    public void showFreeSlot() { /* ... */ }
}
