package org.lld.parkinglot.parkingslot;

import org.lld.parkinglot.vehicle.Vehicle;

public abstract class ParkingSpot {
   private String id;
   private boolean isFree;
   private Vehicle vehicle;
   public abstract boolean parkVehicle(Vehicle vehicle);
   public boolean removeVehicle(Vehicle vehicle) {
       this.isFree =true;
       // some more logic can be added here
       this.vehicle = null;
       return true;
   }


}
