package com.berraktar;

import java.io.Serializable;

public class MessageCreate extends Message implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = 1945523429430420445L;

    // Kérés típusa
    private boolean isIncoming;

    MessageCreate() {
        super();
    }

    // Getterek, setterek
    public boolean isIncoming() {
        return isIncoming;
    }
    public void setIncoming() {
        isIncoming = true;
    }
}
