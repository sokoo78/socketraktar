package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Worksheet implements Serializable {

    // Ez a szerializáláshoz kell
    private static final long serialVersionUID = -7149976626711109237L;

    // Munkalaptípusok
    public enum WorkSheetType {Incoming, Outgoing}

    // Munkalap tulajdonságai
    private int transactionID;                  // Munkalap azonosító - sima futó sorszám
    private final WorkSheetType workSheetType;  // Beszállítás vagy kiszállítás

    // Állapotjelzők
    private boolean isInitialized = false;  // Van már tranzakcióazonosítója
    private boolean isRejected = false;     // Nem teljesíthető az igény
    private boolean isApproved = false;     // Teljesíthető az igény
    private boolean isActive = false;       // Kocsi beérkezett
    private boolean isProcessing = false;   // Rakodás folyamatban
    private boolean isCompleted = false;    // Végrehajtva
    private boolean isCancelled = false;    // Visszamondva

    // Igénylési adatok - diszpécser adja meg
    private String renterID;                // Bérlő
    private LocalDateTime reservedDate;     // Igényelt időpont
    private LocalDateTime receivedDate;     // Beérkezés időpontja
    private String externalPartNumber;      // Vevői cikkszám
    private boolean isCooled;               // Hűtendő vagy normál
    private int numberOfPallets;            // Paletták száma
    private int processedPallets;            // Paletták száma

    // Tárolási adatok
    private List<Integer> locations;
    private int terminalID;

    // Konstruktor
    Worksheet(WorkSheetType worktype) {
        this.workSheetType = worktype;
    }

    // Paletta kipakolás
    Pallet takeOnePallet() {
        Pallet pallet = new Pallet(this.getRenterID(), this.getExternalPartNumber());
        if (this.processedPallets < this.numberOfPallets){
            processedPallets++;
        } else {
            return null; // Nincs több paletta
        }
        return pallet;
    }

    // Paletta kipakolás
    boolean processOnePallet() {
        if (this.processedPallets < this.numberOfPallets){
            processedPallets++;
            return true;
        } else {
            return false; // Nincs több paletta
        }
    }

    // Getterek, Setterek

    String getStatus(){
        String status                  = "Elutasítva  ";
        if (this.isInitialized) status = "Létrehozva  ";
        if (this.isRejected) status    = "Elutasítva  ";
        if (this.isApproved) status    = "Elfogadva   ";
        if (this.isActive) status      = "Aktív       ";
        if (this.isProcessing) status  = "Folyamatban ";
        if (this.isCompleted) status   = "Végrehajtva ";
        if (this.isCancelled) status   = "Visszamondva";
        return status;
    }

    public int getTransactionID() {
        return transactionID;
    }

    void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    WorkSheetType getWorkSheetType() {
        return workSheetType;
    }

    void setInitialized() {
        isInitialized = true;
    }

    void setRejected() {
        isRejected = true;
    }

    boolean isApproved() {
        return isApproved;
    }

    void setApproved() {
        isApproved = true;
    }

    boolean isActive() {
        return isActive;
    }

    void setActive() {
        isActive = true;
    }

    public String getRenterID() {
        return renterID;
    }

    public void setRenterID(String renterID) {
        this.renterID = renterID;
    }

    LocalDateTime getReservedDate() {
        return reservedDate;
    }

    void setReservedDate(LocalDateTime reservedDate) {
        this.reservedDate = reservedDate;
    }

    String getExternalPartNumber() {
        return externalPartNumber;
    }

    void setExternalPartNumber(String externalPartNumber) {
        this.externalPartNumber = externalPartNumber;
    }

    boolean isCooled() {
        return isCooled;
    }

    void updateCooled(boolean isCooled) {
        this.isCooled = isCooled;
    }

    int getNumberOfPallets() {
        return numberOfPallets;
    }

    void setNumberOfPallets(int numberOfPallets) {
        this.numberOfPallets = numberOfPallets;
    }

    void setCompleted() {
        isCompleted = true;
    }

    void setCancelled() {
        isCancelled = true;
    }

    int getTerminalID() {
        return terminalID;
    }

    void setTerminalID(int terminalID) {
        this.terminalID = terminalID;
    }

    List<Integer> getLocations() {
        return locations;
    }

    void setLocations(List<Integer> locations) {
        this.locations = locations;
    }

    void setProcessing() {
        this.isProcessing = true;
    }

}
