package org.lld.parkinglot.vehicle;

import org.lld.parkinglot.Ticket.ParkingTicket;

public abstract class Vehicle {
    private String liscenseNo;
    private ParkingTicket ticket;

    public abstract void assignTicket(ParkingTicket ticket);

}
