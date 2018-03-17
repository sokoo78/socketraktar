package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Worksheet implements Serializable {

    // Ez a szerializáláshoz kell
    private static final long serialVersionUID = -7149976626711109237L;

    // Munkalaptípusok
    public enum WorkSheetType {Incoming, Outgoing, Invalid}
    // Incoming: beérkező áru
    // OutGoing: kimenő áru

    // Tranzakció típusok
    //public enum TransactionType {Initialize, Approve, Confirm, Cancel}
    // Initialize: Új munkalap kérése a szervertől          - a szerver transactionID-val tér vissza, a munkalap létrejön
    // Approve:    Igénylési adatok ellenőrzése a szerveren - a szerver isApproved flaggel tér vissza
    // Confirm:    Végrehajtás jelzése a szervernek         - a szerver isConfirmed flaggel tér vissza, a munkalap megsemmisül
    // Cancel:     Törlési igény küldése a szervernek       - a szerver isCancelled flaggel tér vissza, a munkalap megsemmisül

    // Munkalap tulajdonságai
    private int transactionID;                  // Munkalap azonosító - sima futó sorszám
    //private TransactionType transaction;        // Végrehajtandó művelet
    private final WorkSheetType workSheetType;  // Beszállítás vagy kiszállítás
    private String transactionMessage;          // Utolsó művelettel kapcsolatos információ (pl hibaüzenet)

    // Állapotjelzők
    private boolean isInitialized = false;  // Van már tranzakcióazonosítója
    private boolean isApproved = false;     // Teljesíthető az igény
    private boolean isActive = false;       // Kocsi beérkezett
    private boolean isConfirmed = false;    // Végrehajtva
    private boolean isCancelled = false;    // Végrehajtva

    // Igénylési adatok - diszpécser adja meg
    private String renterID;                // Bérlő
    private LocalDateTime reservedDate;     // Igényelt időpont
    private LocalDateTime receivedDate;     // Beérkezés időpontja
    private String externalID;              // Vevői cikkszám
    private boolean isCooled;               // Hűtendő vagy normál
    private int numberOfPallets;            // Paletták száma

    // Tárolási adatok - szerver adja meg
    private List<Integer> locations;
    private int terminalID;

    // Konstruktor
    public Worksheet(WorkSheetType worktype) {
        this.workSheetType = worktype;
    }

    // Getterek, Setterek

    public String getStatus(){
        String status                  = "Eldobott    ";
        if (this.isInitialized) status = "Létrehozva  ";
        if (this.isApproved) status    = "Elfogadva   ";
        if (this.isActive) status      = "Aktív       ";
        if (this.isConfirmed) status   = "Végrehajtva ";
        if (this.isCancelled) status   = "Visszamondva";
        return status;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public WorkSheetType getWorkSheetType() {
        return workSheetType;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved() {
        isApproved = true;
    }

    public void updateApproved(boolean isApproved){
        this.isCooled = isApproved;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive() {
        isActive = true;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized() {
        isInitialized = true;
    }

    //public TransactionType getTransaction() {
    //    return transaction;
    //}

    //public void setTransaction(TransactionType transaction) {
    //    this.transaction = transaction;
    //}

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

    public void setCooled() {
        isCooled = true;
    }

    public void updateCooled(boolean isCooled) {
        this.isCooled = isCooled;
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

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed() {
        isConfirmed = true;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled() {
        isCancelled = true;
    }

    public int getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(int terminalID) {
        this.terminalID = terminalID;
    }

    public List<Integer> getLocations() {
        return locations;
    }

    public void setLocations(List<Integer> locations) {
        this.locations = locations;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }
}
