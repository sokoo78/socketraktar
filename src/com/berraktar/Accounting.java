package com.berraktar;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Accounting implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = -5771420064864866043L;

    // Bérlők
    private Map<String, Renter> renters;

    // Konstruktor
    public Accounting(){

        // Bérlők adatainak betöltése
        if (new File("Renters.ser").exists()) {
            this.renters = (Map<String, Renter>) Persistency.LoadObject("Renters.ser");
        }
        // Tesztbérlők létrehozása ha nem létezik a fájl
        else {
            this.createTestRenters();
        }
    }

    public int getTotalCooledReservations(){
        return renters.values().stream().mapToInt(i -> i.getRentedCooledLocations()).sum();
    }

    public int getTotalNormalReservations(){
        return renters.values().stream().mapToInt(i -> i.getRentedNormalLocations()).sum();
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
                50,
                0,
                0);

        Renter renter_2 = new Renter(
                "Renter Zoli",
                "REZO",
                20,
                10,
                20,
                10,
                0);

        Renter renter_3 = new Renter(
                "Gazdag Peti",
                "GAPE",
                0,
                50,
                0,
                50,
                0);

        Map<String, Renter> testRenters = new HashMap<>();
        testRenters.put(renter_1.getCode(), renter_1);
        testRenters.put(renter_2.getCode(), renter_2);
        testRenters.put(renter_3.getCode(), renter_3);
        this.setRenters(testRenters);
    }
}
