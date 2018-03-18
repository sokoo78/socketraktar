package com.berraktar;

public class Pallet {
    private String internalPartNumber;
    private String externalPartNumber;
    private String renterID;


    // Konstruktorok
    public Pallet() {}

    public Pallet(String renterID, String externalPartNumber) {
        this.setRenterID(renterID);
        this.setExternalPartNumber(externalPartNumber);
    }

    public Pallet(String internalPartNumber, String externalPartNumber, String renterID) {
        this.internalPartNumber = internalPartNumber;
        this.externalPartNumber = externalPartNumber;
        this.renterID = renterID;
    }

    public Pallet scanPallet(){
        return this;
    }

    public String getInternalPartNumber() {
        return internalPartNumber;
    }

    public void setInternalPartNumber(String internalPartNumber) {
        this.internalPartNumber = internalPartNumber;
    }

    public String getExternalPartNumber() {
        return externalPartNumber;
    }

    public void setExternalPartNumber(String externalPartNumber) {
        this.externalPartNumber = externalPartNumber;
    }

    public String getRenterID() {
        return renterID;
    }

    public void setRenterID(String renterID) {
        this.renterID = renterID;
    }
}
