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
        this.renterID = renterID;
        this.externalPartNumber = externalPartNumber;
    }

    // Getterek, setterek

    synchronized Pallet scanPallet(){
        return this;
    }

    synchronized String getInternalPartNumber() {
        return internalPartNumber;
    }

    synchronized void setInternalPartNumber(String internalPartNumber) {
        this.internalPartNumber = internalPartNumber;
    }

    synchronized String getExternalPartNumber() {
        return externalPartNumber;
    }

    public synchronized String getRenterID() {
        return renterID;
    }

}
