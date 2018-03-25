package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

class MessageReserve extends Message implements Serializable {

    // Szerializáláshoz kell
    private static final long serialVersionUID = -3464794718903762978L;

    // Foglalás tulajdonságai
    private String RenterID;
    private String PartNumber;
    private boolean IsCooled;
    private int Pallets;
    private LocalDateTime ReservationDate;
    private Worksheet.WorkSheetType workSheetType;

    // Konstruktorok
    MessageReserve() {}

    MessageReserve(String renterID, String partNumber, boolean isCooled, int pallets, LocalDateTime reservationDate){
        this.RenterID = renterID;
        this.PartNumber = partNumber;
        this.IsCooled = isCooled;
        this.Pallets = pallets;
        this.ReservationDate = reservationDate;
    }

    // Getterek, setterek

    public String getRenterID() {
        return RenterID;
    }

    public void setRenterID(String renterID) {
        RenterID = renterID;
    }

    public String getPartNumber() {
        return PartNumber;
    }

    public void setPartNumber(String partNumber) {
        PartNumber = partNumber;
    }

    public boolean isCooled() {
        return IsCooled;
    }

    public void setCooled(boolean isCooled) {
        IsCooled = isCooled;
    }

    public int getPallets() {
        return Pallets;
    }

    public void setPallets(int pallets) {
        Pallets = pallets;
    }

    public LocalDateTime getReservationDate() {
        return ReservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        ReservationDate = reservationDate;
    }

    public void setWorkSheetType(Worksheet.WorkSheetType workSheetType) {
        this.workSheetType = workSheetType;
    }

    public Worksheet.WorkSheetType getWorkSheetType() {
        return workSheetType;
    }
}
