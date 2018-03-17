package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

class Reservation implements Serializable {
    // Szerializáláshoz kell
    private static final long serialVersionUID = -3464794718903762978L;

    // Foglalás tulajdonságai
    String RenterID;
    String PartNumber;
    boolean IsCooled;
    int Pallets;
    LocalDateTime ReservationDate;

    // Konstruktorok
    Reservation() {}

    Reservation(String renterID, String partNumber, boolean isCooled, int pallets, LocalDateTime reservationDate){
        this.RenterID = renterID;
        this.PartNumber = partNumber;
        this.IsCooled = isCooled;
        this.Pallets = pallets;
        this.ReservationDate = reservationDate;
    }
}
