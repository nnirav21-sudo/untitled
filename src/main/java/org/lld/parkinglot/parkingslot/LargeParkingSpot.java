package org.lld.parkinglot.parkingslot;

import org.lld.parkinglot.vehicle.Vehicle;

public class LargeParkingSpot extends ParkingSpot {
    @Override
    public boolean parkVehicle(Vehicle vehicle) {
        return true;
    }
}
