package com.berraktar;

import java.util.HashMap;
import java.util.Map;

public final class Accounting {
    private static Map<String, Renter> renters;

    // Konstruktor - TODO: teszt adatok helyett majd fájlból kell betölteni a bérlőket
    public Accounting(){
        this.createTestRenters();
    }

    public static Map<String, Renter> getRenters() {
        return renters;
    }

    private static void setRenters(Map<String, Renter> renters) {
        Accounting.renters = renters;
    }

    // Teszt bérlők
    private void createTestRenters(){
        Renter renter_1 = new Renter(
                "Bérlő Béla",
                "BEBE",
                20,
                0,
                20,
                0,
                0);

        Renter renter_2 = new Renter(
                "Renter Zoli",
                "REZO",
                20,
                0,
                20,
                0,
                0);

        Renter renter_3 = new Renter(
                "Gazdag Peti",
                "GAPE",
                20,
                0,
                20,
                0,
                0);

        Map<String, Renter> testRenters = new HashMap<>();
        testRenters.put("BEBE", renter_1);
        testRenters.put("REZO", renter_2);
        testRenters.put("GAPE", renter_3);
        Accounting.setRenters(testRenters);
    }
}
