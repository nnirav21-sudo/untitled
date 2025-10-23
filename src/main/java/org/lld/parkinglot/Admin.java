package org.lld.parkinglot;

import org.lld.parkinglot.gate.Entrance;
import org.lld.parkinglot.gate.Exit;
import org.lld.parkinglot.parkingslot.ParkingSpot;

public class Admin extends Account{
    public boolean addParkingSpot(ParkingSpot spot) { /* ... */ return true; }
    public boolean addDisplayBoard(DisplayBoard board) { /* ... */ return true; }
    public boolean addEntrance(Entrance entrance) { /* ... */ return true; }
    public boolean addExit(Exit exit) { /* ... */ return true; }

    public boolean resetPassword() { /* ... */ return true; }
}
