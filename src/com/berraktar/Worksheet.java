package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Worksheet implements Serializable {

    // Ez a szerializáláshoz kell
    private static final long serialVersionUID = -7149976626711109237L;

    // Munkalap tulajdonságai
    private final int transactionID;     // Munkalap azonosító - sima futó sorszám
    private final boolean isIncoming;   // Beszállítás vagy kiszállítás

    // Állapotjelzők
    private boolean isApproved;     // Teljesíthető az igény - szerver állítja be
    private boolean isActive;       // Kocsi beérkezett - diszpécser kezeli

    // Igénylési adatok - diszpécser adja meg
    private Renter renter;          // Bérlő
    private LocalDateTime date;     // Igényelt időpont
    private String externalID;      // Vevői cikkszám
    private boolean isCooled;       // Hűtendő vagy normál
    private int numberOfPallets;    // Paletták száma

    // Tárolási adatok - szerver adja meg
    private Location location;
    private Terminal terminal;

    // Konstruktor
    public Worksheet(int transactionID, boolean isIncoming) {
        this.transactionID = transactionID;
        this.isIncoming = isIncoming;
    }
}
