package com.berraktar;

import java.io.Serializable;

public class Pallet implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = 9177749989045794424L;
    // Paletta tulajdonságai
    private String internalPartNumber;
    private String externalPartNumber;
    private String renterID;

    // Konstruktorok
    Pallet() {}

    Pallet(String renterID, String externalPartNumber) {
        this.setRenterID(renterID);
        this.setExternalPartNumber(externalPartNumber);
    }

    public Pallet(String internalPartNumber, String externalPartNumber, String renterID) {
        this.internalPartNumber = internalPartNumber;
        this.externalPartNumber = externalPartNumber;
        this.renterID = renterID;
    }

    public synchronized Pallet scanPallet(){
        return this;
    }

    public synchronized String getInternalPartNumber() {
        return internalPartNumber;
    }

    public synchronized void setInternalPartNumber(String internalPartNumber) {
        this.internalPartNumber = internalPartNumber;
    }

    public synchronized String getExternalPartNumber() {
        return externalPartNumber;
    }

    public synchronized void setExternalPartNumber(String externalPartNumber) {
        this.externalPartNumber = externalPartNumber;
    }

    public synchronized String getRenterID() {
        return renterID;
    }

    public synchronized void setRenterID(String renterID) {
        this.renterID = renterID;
    }
}
