package com.berraktar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Terminal implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = 4217347426722612092L;

    // Terminál tulajdonságai
    private final int id;
    private boolean isFree;
    private Map<String, Pallet> palletList = new HashMap<>();

    // Konstruktor
    Terminal(int id) {
        this.id = id;
    }


    // Műveletek

    public Pallet takePallet(String palletID){
        Pallet _pallet = palletList.get(palletID);
        palletList.remove(palletID);
        return _pallet;
    }

    // Getterek, Setterek

    public Map<String, Pallet> getPalletList() {
        return palletList;
    }

    public void setPalletList(Map<String, Pallet> palletList) {
        this.palletList = palletList;
    }

    public void addPallet(Pallet pallet){
        this.palletList.put(pallet.getInternalPartNumber(), pallet);
    }

    public int getId() {
        return id;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree() {
        isFree = true;
    }

    public void setOccupied() {
        isFree = false;
    }
}
