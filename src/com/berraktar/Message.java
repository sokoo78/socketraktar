package com.berraktar;

import java.io.Serializable;

public class Message implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = 2611041076912112709L;

    // Tranzakciók közös tulajdonságai
    private int transactionID;
    private String transactionMessage;
    private boolean isApproved;

    // Konstruktorok
    Message(int transactionID) {
        this.transactionID = transactionID;
    }

    Message() {

    }

    // Getterek, setterek

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public int getTransactionID() {
        return this.transactionID;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved() {
        isApproved = true;
    }

    public String getTransactionMessage() {
        return transactionMessage;
    }

    public void setTransactionMessage(String transactionMessage) {
        this.transactionMessage = transactionMessage;
    }
}
