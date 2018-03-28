package com.berraktar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

class Terminal implements Serializable {
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


    // Paletta lerakodása a terminálra
    void addPallet(Pallet pallet){
        this.palletList.put(pallet.getInternalPartNumber(), pallet);
    }

    // Paletta kirakodása a terminálról
    Pallet takePallet(String palletID){
        Pallet _pallet = palletList.get(palletID);
        palletList.remove(palletID);
        return _pallet;
    }

    // Terminál getterei és setterei

    Map<String, Pallet> getPalletList() {
        return palletList;
    }

    void setFree() {
        this.palletList.clear();
        this.isFree = true;
    }

    void setOccupied() {
        isFree = false;
    }
}
