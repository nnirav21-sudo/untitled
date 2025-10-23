package org.lld.rental;

import jdk.jfr.Threshold;
import lombok.Data;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class VehicleRentalSystemLLD
{
   enum VehicleType {
       CAR,
       BIKE
   }


   enum BookingStatus {
       BOOKED,
       AVAILABLE,
       INACTIVE
   }


   enum PaymentStatus {
       PAID,
       UNPAID;
   }


   static class PriceEvaluator{
       static Long evaluate(Vehicle v, Long startTime, Long endTime, Long checkoutTime){
           return 200L;
       }
   }


   static class PaymentSystem {
       static PaymentStatus pay(Long price){
           return PaymentStatus.PAID;
       }


       static PaymentStatus refund(Long price){
           return PaymentStatus.PAID;
       }
   }


   @Data
   class Vehicle {
       final String model;
       final String vehicleId;
       final VehicleType type;
       String location;
       Integer price;
       BookingStatus status;


       Vehicle(String model, String vehicleId, VehicleType type)
       {
           this.model = model;
           this.vehicleId = vehicleId;
           this.type = type;
           this.status = BookingStatus.AVAILABLE;
       }


       void setLocation(String location){
           this.location = location;
       }
       void setPrice(Integer price){
           this.price = price;
       }
       void setStatus(BookingStatus status){
           this.status = status;
       }


       String getVehicleId(){
           return this.vehicleId;
       }


       BookingStatus getStatus(){
           return this.status;
       }
   }


   class User {
       final String name;
       final String id;
       String location;


       User(String name, String id){
           this.name = name;
           this.id = id;
       }


       void setLocation(String location){
           this.location = location;
       }
   }


   class Booking {
       final static Long threshold = 1000L;
       final User user;
       final Vehicle vehicle;
       final Long bookingStartTime;
       final String id;
       Long totalPrice;
       Long bookingEndTime;
       Long checkoutTime;
       PaymentStatus paymentStatus;


       Booking(User user, Vehicle vehicle, String id, Long bookingStartTime, long bookingEndTime){
           this.user = user;
           this.vehicle = vehicle;
           this.id = id;
           this.paymentStatus = PaymentStatus.UNPAID;
           this.bookingStartTime = bookingStartTime;
           this.bookingEndTime  = bookingEndTime;
           this.vehicle.setStatus(BookingStatus.BOOKED);
       }


       void cancel() throws Exception {
           Long currTime = System.currentTimeMillis();
           if(currTime - bookingStartTime > threshold){
               throw new Exception("Can not be cancelled now");
           }
           this.vehicle.setStatus(BookingStatus.AVAILABLE);
           this.paymentStatus = PaymentSystem.refund(this.totalPrice);
           System.out.println("Payment Refund status: " + this.paymentStatus.name());
       }


       void pay(){
           Long currTime = System.currentTimeMillis();
           Long totalPrice = PriceEvaluator.evaluate(this.vehicle, this.bookingStartTime, this.bookingEndTime, this.checkoutTime);
           this.paymentStatus = PaymentSystem.pay(totalPrice);
           System.out.println("Payment paid status " + this.paymentStatus.name());
       }
   }




   class BookingService {
       final List<Vehicle> vehicles;
       final List<Booking> bookings;
       BookingService()
       {
           this.vehicles = new ArrayList<>();
           this.bookings = new ArrayList<>();
       }
       void addVehicle(Vehicle vehicle){
           this.vehicles.add(vehicle);
       }


       void remove(Vehicle vehicle){
           vehicles.removeIf(v -> Objects.equals(v.getVehicleId(), vehicle.getVehicleId()));
       }


       List<Vehicle> getAvalVehicles(){
           List<Vehicle> avalVehicles = new ArrayList<>();
           for(Vehicle v: this.vehicles){
               if(v.getStatus() == BookingStatus.AVAILABLE){
                   avalVehicles.add(v);
               }
           }
           return avalVehicles;
       }


       List<Booking> getBookings(User user){
           if(user==null){
               return this.bookings;
           }
           List<Booking> b = new ArrayList<>();
           for(Booking booking: this.bookings){
               if(booking.user == user){
                   b.add(booking);
               }
           }
           return b;
       }


       void bookVehicle(User user, Vehicle vehicle, String id){
           if(vehicle.getStatus() != BookingStatus.AVAILABLE){
               System.out.println("This vehicle is unavailable.");
               return;
           }
           Booking booking = new Booking(user, vehicle, id, 1000L, 2000L);
           this.bookings.add(booking);
       }
   }


   void main() {
       Vehicle v1 = new Vehicle("Maruti", "1", VehicleType.CAR);
       Vehicle v2 = new Vehicle("Suzuki", "2", VehicleType.CAR);


       User u1 = new User("Shubham", "1");
       User u2 = new User("Nishant", "2");


       BookingService bs = new BookingService();
       bs.addVehicle(v1);
       bs.addVehicle(v2);


       bs.bookVehicle(u1, v1, "11");


       List<Booking> b = bs.getBookings(u1);
       System.out.println("Bookings For user: " + u1.name);
       for(Booking booking: b){
           System.out.println(booking.bookingStartTime);
           System.out.println(booking.vehicle.model);
       }


       System.out.println(bs.getAvalVehicles());
       bs.bookVehicle(u2, v1, "11");


   }
}

