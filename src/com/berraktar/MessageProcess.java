package com.berraktar;

import java.io.Serializable;

class MessageProcess extends Message implements Serializable {

    //Szerializációhoz kell
    private static final long serialVersionUID = 7239227608233814602L;

    // Végrehajtáshoz szükséges adatok
    private String renterID;
    private int pallets;
    private String externalPartNumber;
    private String internalPartNumber;
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

    public int getPallets() {
        return pallets;
    }

    public void setPallets(int pallets) {
        this.pallets = pallets;
    }

    public String getExternalPartNumber() {
        return externalPartNumber;
    }

    public void setExternalPartNumber(String externalPartNumber) {
        this.externalPartNumber = externalPartNumber;
    }

    public String getInternalPartNumber() {
        return internalPartNumber;
    }

    public void setInternalPartNumber(String internalPartNumber) {
        this.internalPartNumber = internalPartNumber;
    }

    public void setTerminalID(int terminalID) {
        this.terminalID = terminalID;
    }

    public int getTerminalID() {
        return terminalID;
    }

}
