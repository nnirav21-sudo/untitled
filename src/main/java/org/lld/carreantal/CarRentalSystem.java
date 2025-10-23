package org.lld.carreantal;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

public class CarRentalSystem {
    List<Store> stores;
    List<Customer> customers;

    Optional<Store> getStore(String id) {
        return stores.stream().filter(store -> store.getStoreId().equals(id))
                .findFirst();
    }
    boolean addStore(Store store) {
        stores.add(store);
        return true;
    }
    boolean addCustomer(Customer customer) {
        customers.add(customer);
        return true;
    }
    List<Vehicle> findAllVehicles(String storeId) {
       for (Store store : stores) {
           if(store.getStoreId().equals(storeId)) {
               return store.getVehicles();
           }
       }
       return null;
    }
    List<Vehicle>  findAvailableVehicles(String storeId) {
        for(Store store : stores) {
            if(store.getStoreId().equals(storeId)) {
                return store.findActiveVehicles();
            }
        }
        return null;
    }
    Reservation createReservation(String storeId, String vehicleId,String customerId,Long startTime,Long endTime) {
        for(Store store : stores) {
            if(store.getStoreId().equals(storeId)) {
                return store.bookVehicle(vehicleId,startTime,endTime,customerId);
            }
        }
        return null;
    }
    Invoice createInvoice(Reservation reservation) {
        Invoice invoice = new Invoice(reservation);
    }


}

@Data
class Vehicle{
    String vehicleId;
    String vehicleRegNumber;
    String modelNo;
    String color;
    int distanceTravelled;
    double hourlyPrice;
    double dailyPrice;
    Date yearOfManufacture;
    VehicleType vehicleType;
    VehicleStatus vehicleStatus;
}

enum VehicleType{
    CAR,BIKE,TRAVELLER,CYCLE;
}
enum  VehicleStatus{
    ACTIVE,INACTIVE;
}
enum ReservationStatus{
    BOOKED,ONGOING,COMPLETED,DELAYED,CANCELLED;
}

class Location{
    String pinCode;
    String address;
    String city;
    String state;
    String country;

}
@Data
class Store {
    String StoreId;
    String StoreName;
    Location location;
    List<Vehicle> vehicles;
    List<Reservation> reservations;
    List<Invoice> invoices;

    List<Vehicle> findActiveVehicles() {
        Set<String> bookedVehicles = reservations.stream()
                .filter(reservation ->
                        reservation.getReservationStatus() == ReservationStatus.BOOKED ||
                                reservation.getReservationStatus() == ReservationStatus.ONGOING ||
                                reservation.getReservationStatus() == ReservationStatus.DELAYED
                )
                .map(Reservation::getVehicleId)
                .collect(Collectors.toSet());
        return vehicles.stream().filter(vehicle -> !bookedVehicles.contains(vehicle.getVehicleId())).toList();
    }
    boolean addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        return true;
    }
    Reservation bookVehicle(String vehicleId,Long startTime,Long endTime,String customerId){
        Reservation reservation = new Reservation(vehicleId,customerId,startTime,endTime );
        reservations.add(reservation);
        return reservation;
    }
}
@Data
class Reservation{
    String reservationId ;
    Long startTime;
    Long endTime;
    Long dropOffTime;
    String userId;
    String vehicleId;
    ReservationStatus reservationStatus;

    public Reservation( String vehicleId, String customerId, Long startTime, Long endTime) {
        this.createNewReservation(vehicleId,customerId,startTime,endTime);
    }

    void createNewReservation(String vehicleId,String customerId,Long startTime,Long endTime){
        this.reservationId =  UUID.randomUUID().toString();
        this.startTime = startTime;
        this.endTime = endTime;
        this.userId = customerId;
        this.vehicleId = vehicleId;
        this.reservationStatus = ReservationStatus.BOOKED;
    }
}

class Invoice{
    String invoiceId;
    String invoiceNumber;
    String invoiceDate;
    String invoiceType;
    boolean invoiceStatus;
    String reservationId;

    public Invoice(Reservation reservation) {
        this.createInvoice(reservation);
    }
    void createInvoice(Reservation reservation) {
        this.invoiceId = UUID.randomUUID().toString();

    }
    double calculateInvoicePrice(Reservation reservation){

    }
}
class Customer{
    String customerId;
    String customerName;
    String customerAddress;
    String customerPhone;
    String customerEmail;
    List<Reservation> reservations;
}

