package com.berraktar;

import java.io.Serializable;

public class CreateWorkMessage extends Message implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = 1945523429430420445L;

    // Kérés típusa
    private boolean isIncoming;

    // Getterek, setterek
    public boolean isIncoming() {
        return isIncoming;
    }
    public void setIncoming() {
        isIncoming = true;
    }
}
