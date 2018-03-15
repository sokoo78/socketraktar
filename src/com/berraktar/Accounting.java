package com.berraktar;

import java.util.HashMap;
import java.util.Map;

public class Accounting {
    private Map<String, Renter> renters;

    // Konstruktor - TODO: teszt adatok helyett majd fájlból kell betölteni a bérlőket
    public Accounting(){
        this.createTestRenters();
    }

    public Renter getRenter(String renterID){
        Renter renter = this.renters.get(renterID);
        return renter;
    }

    public Map<String, Renter> getRenters() {
        return this.renters;
    }

    private void setRenters(Map<String, Renter> renters) {
        this.renters = renters;
    }

    // Teszt bérlők
    private void createTestRenters(){
        Renter renter_1 = new Renter(
                "Bérlő Béla",
                "BEBE",
                50,
                0,
                20,
                0,
                0);

        Renter renter_2 = new Renter(
                "Renter Zoli",
                "REZO",
                20,
                10,
                20,
                0,
                0);

        Renter renter_3 = new Renter(
                "Gazdag Peti",
                "GAPE",
                0,
                50,
                0,
                5,
                0);

        Map<String, Renter> testRenters = new HashMap<>();
        testRenters.put(renter_1.getCode(), renter_1);
        testRenters.put(renter_2.getCode(), renter_2);
        testRenters.put(renter_3.getCode(), renter_3);
        this.setRenters(testRenters);
    }
}
