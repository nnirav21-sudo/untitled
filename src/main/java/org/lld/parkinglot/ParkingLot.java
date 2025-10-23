package org.lld.parkinglot;

import org.lld.parkinglot.Ticket.ParkingTicket;
import org.lld.parkinglot.data.Address;
import org.lld.parkinglot.gate.Entrance;
import org.lld.parkinglot.gate.Exit;
import org.lld.parkinglot.parkingslot.ParkingSpot;
import org.lld.parkinglot.vehicle.Vehicle;

import java.util.List;
import java.util.Map;

public class ParkingLot {
    private int id;
    private String name;
    private Address address;
    private ParkingRate parkingRate;

    private Map<String, Entrance> entrances;
    private Map<String, Exit> exits;
    private Map<Integer, ParkingSpot> spots;
    private Map<String, ParkingTicket> tickets;
    private List<DisplayBoard> displayBoards;

    // Singleton implementation
    private static ParkingLot parkingLot = null;
    private ParkingLot() { /* ... */ }
    public static ParkingLot getInstance() {
        if (parkingLot == null) { parkingLot = new ParkingLot(); }
        return parkingLot;
    }

    public boolean addEntrance(Entrance entrance) { /* ... */ return true; }
    public boolean addExit(Exit exit) { /* ... */ return true; }
    public boolean addParkingSpot(ParkingSpot spot) { /* ... */ return true; }
    public boolean addDisplayBoard(DisplayBoard board) { /* ... */ return true; }

    public ParkingTicket getParkingTicket(Vehicle vehicle) { /* ... */ return null; }
    public boolean isFull(ParkingSpot spotType) { /* ... */ return false; }
}
