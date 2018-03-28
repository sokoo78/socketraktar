package com.berraktar;

import java.io.Serializable;

class MessageShip extends Message implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = -4292462495046993569L;

    // Konstruktor
    MessageShip(int transactionID) {
        super(transactionID);
    }
}
