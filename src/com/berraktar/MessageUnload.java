package com.berraktar;

import java.io.Serializable;

class MessageUnload extends Message implements Serializable {

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

    String getInternalPartNumber() {
        return internalPartNumber;
    }

    void setTerminalID(int terminalID) {
        this.terminalID = terminalID;
    }

    int getTerminalID() {
        return terminalID;
    }
}
