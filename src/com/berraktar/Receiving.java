package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

class Receiving implements Serializable {

    //Szerializációhoz kell
    private static final long serialVersionUID = 7239227608233814602L;

    // Beérkezés tulajdonságai
    private int transactionID;
    private String transactionMessage;
    private LocalDateTime receivingDate;
    private boolean isApproved;

    // Konstruktor
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

    public void updateReceivingDate(LocalDateTime receivingDate) {
        this.receivingDate = receivingDate;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved() {
        this.isApproved = true;
    }

    public void updateApproved(boolean isApproved) {
        this.isApproved = isApproved;
    }

    public String getTransactionMessage() {
        return transactionMessage;
    }

    public void setTransactionMessage(String transactionMessage) {
        this.transactionMessage = transactionMessage;
    }
}
