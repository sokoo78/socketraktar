package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Reservation implements Serializable {
    // Szerializáláshoz kell
    private static final long serialVersionUID = -3464794718903762978L;

    // Foglalás tulajdonságai
    public String RenterID;
    public String PartNumber;
    public boolean IsCooled;
    public int Pallets;
    public LocalDateTime ReservationDate;

    // Konstruktor
    public Reservation(String renterID, String partNumber, boolean isCooled, int pallets, LocalDateTime reservationDate){
        this.RenterID = renterID;
        this.PartNumber = partNumber;
        this.IsCooled = isCooled;
        this.Pallets = pallets;
        this.ReservationDate = reservationDate;
    }
}
