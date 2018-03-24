package com.berraktar;

import java.io.Serializable;

public class UnloadingMessage implements Serializable {
    private static final long serialVersionUID = -4292462495046993569L;

    // Művelet tulajdonságai
    private int transactionID;
    private String internalPartNumber;


    // Művelet állapota
    private boolean isConfirmed;
    private String transactionMessage;


    // Konstruktor
    UnloadingMessage(int transactionID, String scannedPartNumber) {
        this.transactionID = transactionID;
        this.internalPartNumber = scannedPartNumber;
    }

    // Getterek, setterek

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed() {
        isConfirmed = true;
    }

    public void setTransactionMessage(String transactionMessage) {
        this.transactionMessage = transactionMessage;
    }

    public String getTransactionMessage() {
        return transactionMessage;
    }

    public String getInternalPartNumber() {
        return internalPartNumber;
    }

    public void setInternalPartNumber(String internalPartNumber) {
        this.internalPartNumber = internalPartNumber;
    }
}
