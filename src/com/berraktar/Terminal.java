package com.berraktar;

import java.util.List;

public class Terminal {
    private final int id;
    private final boolean isCooled;
    private boolean isFree;
    private List<Pallet> pallets;

    public Terminal(int id, boolean isCooled) {
        this.id = id;
        this.isCooled = isCooled;
        this.isFree = true;
    }

    public boolean isFree() {
        return isFree;
    }

    private void setFree() {
        this.isFree = true;
    }

    public void setReserved() {
        this.isFree = false;
    }

    public List<Pallet> getPallets() {
        return pallets;
    }

    public void setPallets(List<Pallet> pallets) {
        this.pallets = pallets;
    }

    public void shipPallets(){
        this.pallets = null;
        this.setFree();
    }
}
