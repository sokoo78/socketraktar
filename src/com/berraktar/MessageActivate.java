package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MessageActivate extends Message implements Serializable {

    // Szerializáláshoz kell
    private static final long serialVersionUID = -3718326739685596202L;

    // Aktiváláshoz kell
    private LocalDateTime receivingDate;

    // Konstruktor
    MessageActivate(int transactionID, LocalDateTime receivingDate) {
        super(transactionID);
        this.receivingDate = receivingDate;
    }

    // Getterek, setterek

    public LocalDateTime getReceivingDate() {
        return receivingDate;
    }
}
