package com.berraktar;

import java.util.List;

public class Terminal {
    private final int id;
    private List<Integer> palletIDList;

    public Terminal(int id) {
        this.id = id;
    }

    public List<Integer> getPallets() {
        return palletIDList;
    }

    public void setPallets(List<Integer> pallets) {
        this.palletIDList = pallets;
    }

    public void shipPallets(){
        this.palletIDList.clear();
    }

    public void addPallet(int palletID){
        this.palletIDList.add(palletID);
    }
}
