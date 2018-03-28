package com.berraktar;

import java.io.Serializable;

public class MessageStore extends Message implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = 7591961149740222548L;
    private String renterID;
    private String internalPartNumber;

    // Konstruktor
    MessageStore(int transactionID) {
        super(transactionID);
    }

    // Getterek, setterek

    public String getRenterID() {
        return renterID;
    }

    public void setRenterID(String renterID) {
        this.renterID = renterID;
    }

    public String getInternalPartNumber() {
        return internalPartNumber;
    }

    public void setInternalPartNumber(String internalPartNumber) {
        this.internalPartNumber = internalPartNumber;
    }
}
