package com.berraktar;

public class Location {
    private final int locationId;
    private final boolean isCooled;
    private boolean isReserved;
    private Pallet pallet;

    public Location(int id, boolean isCooled){
        this.locationId = id;
        this.isCooled = isCooled;
        this.pallet = null;
        this.isReserved = false;
    }

    public boolean isCooled() {
        return isCooled;
    }

    public void setReserved(){
        this.isReserved = true;
    }

    public void setFree(){
        this.isReserved = false;
    }

    public Pallet scanPallet() {
        return pallet;
    }

    public void addPallet(Pallet pallet) {
        this.setReserved();
        this.pallet = pallet;
    }

    public Pallet takePallet() {
        this.setFree();
        Pallet _pallet = this.pallet;
        this.pallet = null;
        return _pallet;
    }
}
