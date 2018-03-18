package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

class Reservation implements Serializable {

    // Szerializáláshoz kell
    private static final long serialVersionUID = -3464794718903762978L;

    // Foglalás tulajdonságai
    private int TransactionID = 0;
    private String TransactionMessage;
    private Worksheet.WorkSheetType WorkSheetType;
    private String RenterID;
    private String PartNumber;
    private boolean IsCooled;
    private int Pallets;
    private LocalDateTime ReservationDate;

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

    public void setCreated(){
        this.isCreated = true;
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

    public void setApproved() {
        isApproved = true;
    }

    public String getTransactionMessage() {
        return TransactionMessage;
    }

    public void setTransactionMessage(String transactionMessage) {
        TransactionMessage = transactionMessage;
    }

    public String getPartNumber() {
        return PartNumber;
    }

    public String getRenterID() {
        return RenterID;
    }

    public int getPallets() {
        return Pallets;
    }

    public boolean isCooled() {
        return IsCooled;
    }

    public void setWorkSheetType(Worksheet.WorkSheetType workSheetType) {
        WorkSheetType = workSheetType;
    }

    public void setRenterID(String renterID) {
        RenterID = renterID;
    }

    public void setPartNumber(String partNumber) {
        PartNumber = partNumber;
    }

    public void setCooled(boolean isCooled) {
        IsCooled = isCooled;
    }

    public void setPallets(int pallets) {
        Pallets = pallets;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        ReservationDate = reservationDate;
    }
}
