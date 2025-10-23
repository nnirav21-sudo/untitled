package org.lld.parkinglot;

import org.lld.parkinglot.parkingslot.ParkingSpot;
import org.lld.parkinglot.vehicle.Vehicle;

class ParkingRate {
    private double hours;
    private double rate;

    public double calculate(double duration, Vehicle vehicle, ParkingSpot spot) {
        // Pricing logic can use duration, vehicle type, spot type, etc.
        return 0.0;
    }
}
