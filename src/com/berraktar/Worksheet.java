package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Worksheet implements Serializable {

    // Ez a szerializáláshoz kell
    private static final long serialVersionUID = -7149976626711109237L;

    // Munkalaptípusok
    public enum WorkType {Incoming, Outgoing}
    public enum TransactionType {Initialize, Approve, Activate, Confirm, Cancel}

    // Munkalap tulajdonságai
    private int transactionID;              // Munkalap azonosító - sima futó sorszám
    private final WorkType workType;      // Beszállítás vagy kiszállítás
    private TransactionType transaction;    // Végrehajtandó művelet

    // Állapotjelzők
    private boolean isInitialized = false;  // Van már tranzakcióazonosítója
    private boolean isApproved = false;     // Teljesíthető az igény
    private boolean isActive = false;       // Kocsi beérkezett
    private boolean isConfirmed = false;    // Végrehajtva

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
    public Worksheet(WorkType worktype) {
        this.workType = worktype;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized() {
        isInitialized = true;
    }

    public TransactionType getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionType transaction) {
        this.transaction = transaction;
    }
}
