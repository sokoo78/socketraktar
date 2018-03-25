package com.berraktar;

import java.io.Serializable;

public class MessageUnload extends Message implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = -4292462495046993569L;

    // Művelet tulajdonságai
    private String internalPartNumber;
    private int terminalID;

    // Konstruktor
    MessageUnload(int transactionID, String scannedPartNumber) {
        super(transactionID);
        this.internalPartNumber = scannedPartNumber;
    }

    // Getterek, setterek

    public String getInternalPartNumber() {
        return internalPartNumber;
    }

    public void setTerminalID(int terminalID) {
        this.terminalID = terminalID;
    }

    public int getTerminalID() {
        return terminalID;
    }
}
