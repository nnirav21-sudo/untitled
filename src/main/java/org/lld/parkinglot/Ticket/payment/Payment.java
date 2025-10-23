package org.lld.parkinglot.Ticket.payment;

import org.lld.parkinglot.data.PaymentStatus;

import java.util.Date;

public abstract class Payment {
    private double amount;
    private PaymentStatus status;
    private Date timestamp;

    public abstract boolean initiateTransaction();
}
