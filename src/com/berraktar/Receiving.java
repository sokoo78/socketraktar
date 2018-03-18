package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

class Receiving implements Serializable {

    //Szerializációhoz kell
    private static final long serialVersionUID = 7239227608233814602L;

    // Aktiváláshoz szükséges adatok
    private int transactionID;
    private String transactionMessage;
    private LocalDateTime receivingDate;
    private boolean isApproved;
    private boolean isProcessing;
    private boolean isUnloaded;
    private boolean isConfirmed;
    private boolean isCompleted;

    // Végrehajtáshoz szükséges adatok
    private String renterID;
    private int pallets;
    private String externalPartNumber;
    private String internalPartNumber;
    private int terminalID;
    private List<Integer> locations;

    // Konstruktorok

    Receiving(int transactionID) {
        this.transactionID = transactionID;
    }

    Receiving(int transactionID, LocalDateTime receivingDate) {
    this.transactionID = transactionID;
    this.receivingDate = receivingDate;
    }

    // Getterek, setterek

    public int getTransactionID() {
        return transactionID;
    }

    public LocalDateTime getReceivingDate() {
        return receivingDate;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved() {
        this.isApproved = true;
    }

    public String getTransactionMessage() {
        return transactionMessage;
    }

    public void setTransactionMessage(String transactionMessage) {
        this.transactionMessage = transactionMessage;
    }

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

    public void setTerminalID(int terminalID) {
        this.terminalID = terminalID;
    }

    public void setLocations(List<Integer> locations) {
        this.locations = locations;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed() {
        isConfirmed = true;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public void setProcessing() {
        isProcessing = true;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted() {
        isCompleted = true;
    }

    public boolean isUnloaded() {
        return isUnloaded;
    }

    public void setUnloaded() {
        isUnloaded = true;
    }

    public int getTerminalID() {
        return terminalID;
    }

    public String getInternalPartNumber() {
        return internalPartNumber;
    }

    public void setInternalPartNumber(String internalPartNumber) {
        this.internalPartNumber = internalPartNumber;
    }
}
