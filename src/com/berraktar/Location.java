package com.berraktar;

import java.io.Serializable;

public class Location implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = 4083927560199725128L;

    // Lokáció tulajdonságai
    private final int locationID;
    private final String RenterID;
    private Pallet pallet = null;

    // Lokáció állapota
    private boolean isReserved = false;

    // Konstruktor
    Location(int locationID, String renterID, boolean isReserved){
        this.locationID = locationID;
        this.RenterID = renterID;
        this.isReserved = isReserved;
    }

    public Pallet scanPallet(){
        return this.pallet.scanPallet();
    }

    public String scanPalletInternalID(){
        if (this.pallet == null){
            return "n/a";
        } else {
            return this.pallet.getInternalPartNumber();
        }
    }

    public String scanPalletExternalID(){
        if (this.pallet == null){
            return "n/a";
        } else {
            return this.pallet.getExternalPartNumber();
        }
    }

    public String scanPalletRenterID(){
        if (this.pallet == null){
            return "n/a";
        } else {
            return this.pallet.getRenterID();
        }
    }

    public void addPallet(Pallet pallet) {
        this.pallet = pallet;
    }

    public Pallet takePallet() {
        Pallet _pallet = this.pallet;
        this.pallet = null;
        return _pallet;
    }

    public int getLocationID() {
        return locationID;
    }

    public String  getRenterID() {
        return RenterID;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved() {
        isReserved = true;
    }

    public void updateReserved(boolean reserved) {
        isReserved = reserved;
    }
}
