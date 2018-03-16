package com.berraktar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Terminal implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = 4217347426722612092L;

    // Terminál tulajdonságai
    private final int id;
    private Map<String, Pallet> palletList = new HashMap<>();

    // Konstruktor
    public Terminal(int id) {
        this.id = id;
    }

    // Getterek, Setterek

    public Map<String, Pallet> getPalletList() {
        return palletList;
    }

    public void setPalletList(Map<String, Pallet> palletList) {
        this.palletList = palletList;
    }

    public void addPallet(Pallet pallet){
        palletList.put(pallet.getInternalPartNumber(), pallet);
    }

    public Pallet takePallet(String palletID){
        Pallet _pallet = palletList.get(palletID);
        palletList.remove(palletID);
        return _pallet;
    }

    public int getId() {
        return id;
    }
}
