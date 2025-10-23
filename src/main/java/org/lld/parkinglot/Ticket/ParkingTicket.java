package org.lld.parkinglot.Ticket;

import org.lld.parkinglot.Ticket.payment.Payment;
import org.lld.parkinglot.data.TicketStatus;
import org.lld.parkinglot.gate.Entrance;
import org.lld.parkinglot.gate.Exit;
import org.lld.parkinglot.vehicle.Vehicle;

import java.util.Date;

public class  ParkingTicket {
    private int ticketNo;
    private Date entryTime;
    private Date exitTime;
    private double amount;
    private TicketStatus status;

    private Vehicle vehicle;
    private Payment payment; // Composition: Ticket owns Payment
    private Entrance entrance;
    private Exit exitIns;
}
