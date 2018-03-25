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
    private int unloadedPallets;            // Paletták száma

    // Tárolási adatok - szerver adja meg
    private List<Integer> locations;
    private int terminalID;

    // Konstruktor
    Worksheet(WorkSheetType worktype) {
        this.workSheetType = worktype;
    }

    // Műveletek

    // Paletta kipakolás
    public Pallet takePallet () {
        Pallet pallet = new Pallet(this.getRenterID(), this.getExternalPartNumber());
        System.out.println("\n" +  this.numberOfPallets + " fos " + this.unloadedPallets + "\n");
        if (this.unloadedPallets < this.numberOfPallets){
            unloadedPallets++;
        } else {
            return null; // Nincs több paletta
        }
        return pallet;
    }

    // Getterek, Setterek

    public String getStatus(){
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

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public WorkSheetType getWorkSheetType() {
        return workSheetType;
    }

    public void setInitialized() {
        isInitialized = true;
    }

    public void setRejected() {
        isRejected = true;
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

    public void setActive() {
        isActive = true;
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

    public String getExternalPartNumber() {
        return externalPartNumber;
    }

    public void setExternalPartNumber(String externalPartNumber) {
        this.externalPartNumber = externalPartNumber;
    }

    public boolean isCooled() {
        return isCooled;
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

    public void setConfirmed() {
        isCompleted = true;
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

    public void setProcessing() {
        this.isProcessing = true;
    }

}
