package com.berraktar;

import java.io.Serializable;
import java.util.List;

class MessageProcess extends Message implements Serializable {

    //Szerializációhoz kell
    private static final long serialVersionUID = 7239227608233814602L;

    // Végrehajtáshoz szükséges adatok
    private String renterID;
    private int pallets;
    private String externalPartNumber;
    private String internalPartNumber;
    private List<Integer> locations;
    private int terminalID;

    // Konstruktorok

    MessageProcess(int transactionID) {
        super(transactionID);
    }

    // Getterek, setterek

    public String getRenterID() {
        return renterID;
    }

    public void setRenterID(String renterID) {
        this.renterID = renterID;
    }

    int getPallets() {
        return pallets;
    }

    void setPallets(int pallets) {
        this.pallets = pallets;
    }

    String getExternalPartNumber() {
        return externalPartNumber;
    }

    void setExternalPartNumber(String externalPartNumber) {
        this.externalPartNumber = externalPartNumber;
    }

    String getInternalPartNumber() {
        return internalPartNumber;
    }

    void setInternalPartNumber(String internalPartNumber) {
        this.internalPartNumber = internalPartNumber;
    }

    void setTerminalID(int terminalID) {
        this.terminalID = terminalID;
    }

    int getTerminalID() {
        return terminalID;
    }

    public List<Integer> getLocations() {
        return locations;
    }

    void setLocations(List<Integer> locations) {
        this.locations = locations;
    }
}
