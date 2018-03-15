package com.berraktar;

public class Location {
    private final int locationID;
    private Pallet pallet = new Pallet();

    // Konstruktor
    public Location(int id){
        this.locationID = id;
    }

    public Pallet scanPallet(){
        return this.pallet.scanPallet();
    }

    public void addPallet(Pallet pallet) {
        this.pallet = pallet;
    }

    public Pallet takePallet() {
        Pallet _pallet = this.pallet;
        this.pallet = null;
        return _pallet;
    }
}
