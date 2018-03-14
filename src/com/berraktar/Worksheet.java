package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Worksheet implements Serializable {

    // Ez a szerializáláshoz kell
    private static final long serialVersionUID = -7149976626711109237L;

    // Munkalaptípusok
    public enum WorkType {Incoming, Outgoing}
    // Incoming: beérkező áru
    // OutGoing: kimenő áru

    // Tranzakció flagek
    public enum TransactionType {Initialize, Approve, Activate, Confirm, Cancel}
    // Initialize: Új munkalap kérése a szervertől          - a szerver transactionID-val tér vissza, a munkalap létrejön
    // Approve:    Igénylési adatok ellenőrzése a szerveren - a szerver isApproved flaggel tér vissza
    // Activate:   Beérkezés jelzése a szervernek           - a szerver isActive flaggel tér vissza
    // Confirm:    Végrehajtás jelzése a szervernek         - a szerver isConfirmed flaggel tér vissza, a munkalap megsemmisül
    // Cancel:     Törlési igény küldése a szervernek       - a szerver isCancelled flaggel tér vissza, a munkalap megsemmisül

    // Munkalap tulajdonságai
    private int transactionID;              // Munkalap azonosító - sima futó sorszám
    private final WorkType workType;        // Beszállítás vagy kiszállítás
    private TransactionType transaction;    // Végrehajtandó művelet
    private String transactionMessage;      // Utolsó művelettel kapcsolatos információ (pl hibaüzenet)

    // Állapotjelzők
    private boolean isInitialized = false;  // Van már tranzakcióazonosítója
    private boolean isApproved = false;     // Teljesíthető az igény
    private boolean isActive = false;       // Kocsi beérkezett
    private boolean isConfirmed = false;    // Végrehajtva
    private boolean isCancelled = false;    // Végrehajtva

    // Igénylési adatok - diszpécser adja meg
    private String renterID;                // Bérlő
    private LocalDateTime reservedDate;     // Igényelt időpont
    private String externalID;              // Vevői cikkszám
    private boolean isCooled;               // Hűtendő vagy normál
    private int numberOfPallets;            // Paletták száma

    // Tárolási adatok - szerver adja meg
    private Location location;
    private Terminal terminal;

    // Konstruktor
    public Worksheet(WorkType worktype) {
        this.workType = worktype;
    }

    // Getterek, Setterek
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

    public void setApproved() {
        isApproved = true;
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

    public String getRenterID() {
        return renterID;
    }

    public void setRenterID(String renterID) {
        this.renterID = renterID;
    }

    public LocalDateTime getReservedDate() {
        return reservedDate;
    }

    public void setReservedDate(LocalDateTime reservedDate) {
        this.reservedDate = reservedDate;
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public boolean isCooled() {
        return isCooled;
    }

    public void setCooled(boolean cooled) {
        isCooled = cooled;
    }

    public int getNumberOfPallets() {
        return numberOfPallets;
    }

    public void setNumberOfPallets(int numberOfPallets) {
        this.numberOfPallets = numberOfPallets;
    }

    public String getTransactionMessage() {
        return transactionMessage;
    }

    public void setTransactionMessage(String transactionMessage) {
        this.transactionMessage = transactionMessage;
    }
}
