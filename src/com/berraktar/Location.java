package com.berraktar;

public class Location {
    private final int locationID;
    private int palletID = 0;

    public Location(int id){
        this.locationID = id;
    }

    public int getPalletID() {
        return palletID;
    }

    public void addPallet(int palletID) {
        this.palletID = palletID;
    }

    public int takePallet() {
        int _palletID = this.palletID;
        this.palletID = 0;
        return _palletID;
    }
}
