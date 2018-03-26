package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MessageOrder extends Message implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = -8058992927531055002L;

    // Rendeléshez kell
    private String RenterID;
    private int Pallets;
    private String PartNumber;
    private LocalDateTime ReservationDate;

    // Konstruktorok
    MessageOrder(String renterID, String partNumber, int pallets, LocalDateTime reservationDate) {
        this.RenterID = renterID;
        this.Pallets = pallets;
        this.PartNumber = partNumber;
        this.ReservationDate = reservationDate;
    }

    MessageOrder() {}

    // Getterek, setterek

    public String getRenterID() {
        return RenterID;
    }

    public void setRenterID(String renterID) {
        this.RenterID = renterID;
    }

    public int getPallets() {
        return Pallets;
    }

    public void setPallets(int pallets) {
        this.Pallets = pallets;
    }

    public String getPartNumber() {
        return PartNumber;
    }

    public void setPartNumber(String partNumber) {
        PartNumber = partNumber;
    }

    public LocalDateTime getReservationDate() {
        return ReservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        ReservationDate = reservationDate;
    }
}
