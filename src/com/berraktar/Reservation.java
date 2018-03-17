package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

class Reservation implements Serializable {

    // Szerializáláshoz kell
    private static final long serialVersionUID = -3464794718903762978L;

    // Foglalás tulajdonságai
    private int TransactionID = 0;
    private String TransactionMessage;
    Worksheet.WorkSheetType WorkSheetType;
    String RenterID;
    String PartNumber;
    boolean IsCooled;
    int Pallets;
    LocalDateTime ReservationDate;

    // Állapotjelzők
    private boolean isCreated = false;
    private boolean isApproved = false;

    // Konstruktorok
    Reservation() {}

    Reservation(String renterID, String partNumber, boolean isCooled, int pallets, LocalDateTime reservationDate){
        this.RenterID = renterID;
        this.PartNumber = partNumber;
        this.IsCooled = isCooled;
        this.Pallets = pallets;
        this.ReservationDate = reservationDate;
    }

    // Getterek, setterek

    public int getTransactionID() {
        return this.TransactionID;
    }

    public void setTransactionID(int transactionID){
        this.TransactionID = transactionID;
    }

    public boolean isCreated() {
        return this.isCreated;
    }

    public Worksheet.WorkSheetType getWorkSheetType() {
        return WorkSheetType;
    }

    public LocalDateTime getReservationDate() {
        return ReservationDate;
    }

    public boolean isApproved() {
        return this.isApproved;
    }

    public String getTransactionMessage() {
        return TransactionMessage;
    }

    public void setTransactionMessage(String transactionMessage) {
        TransactionMessage = transactionMessage;
    }
}
