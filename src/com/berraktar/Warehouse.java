package com.berraktar;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Warehouse implements Serializable {
    // Szerializációhoz kell
    private final long serialVersionUID = 1041441426752225702L;

    // Raktár tulajdonságai
    private final int maxNormalLocation;   // Normál lokációk maximális száma
    private final int maxCooledLocation;   // Hűtött lokációk maximális száma
    private final int maxNormalTerminal;   // Normál terminálok maximális száma
    private final int maxCooledTerminal;   // Hűtött terminálok maximális száma

    // Raktározási adatok
    private ConcurrentHashMap<Integer,Location> normalLocations = new ConcurrentHashMap<>();   // Normál lokáció objektumok
    private ConcurrentHashMap<Integer,Location> cooledLocations = new ConcurrentHashMap<>();   // Hűtött lokáció objektumok
    private ConcurrentHashMap<Integer,Terminal> normalTerminals = new ConcurrentHashMap<>();   // Normál terminál objektumok
    private ConcurrentHashMap<Integer,Terminal> cooledTerminals = new ConcurrentHashMap<>();   // Hűtött terminál objektumok
    private ConcurrentHashMap<Integer, Worksheet> worksheets = new ConcurrentHashMap<>();       // Munkalapok

    // Munkalap számláló
    private AtomicInteger workCounter;

    public Warehouse(int maxNormalLocation, int maxCooledLocation, int maxNormalTerminal, int maxCooledTerminal){
        this.maxNormalLocation = maxNormalLocation;
        this.maxCooledLocation = maxCooledLocation;
        this.maxNormalTerminal = maxNormalTerminal;
        this.maxCooledTerminal = maxCooledTerminal;

        // Adatok betöltése
        if (new File("WorkSheets.ser").exists()) {
            this.worksheets = (ConcurrentHashMap<Integer, Worksheet>) Persistency.LoadObject("WorkSheets.ser");
        }
        if (new File("WorkCounter.ser").exists()) {
            this.workCounter = (AtomicInteger) Persistency.LoadObject("WorkCounter.ser");
        }
        else {
            workCounter = new AtomicInteger(0);
        }
    }

    // Új munkalap létrehozása
    public Worksheet CreateWorkSheet(Worksheet.WorkType workType) {
        Integer newID = workCounter.incrementAndGet();
        Worksheet newWorkSheet = new Worksheet(workType);
        newWorkSheet.setTransactionID(newID);
        newWorkSheet.setInitialized();
        worksheets.put(newID, newWorkSheet);
        return newWorkSheet;
    }

    // Munkalap adatainak ellenőrzése, előfoglalás
    public Worksheet ApproveWorkSheet(Worksheet worksheet) {
        boolean checksPassed = true;
        String errorMessage = "szar van a palacsintában";
        // TODO: 1. Bérlőt ellenőrizni, hogy létezik-e --> Accounting osztály --> getRenter(String renter)
        // TODO: 2. Bérlő szabad kapacitását ellenőrizni, hogy teljesíthető-e az igény --> Renter osztály --> getFreeSpace(int noOfPallets, bool isCooled)
        // TODO: 3. Ha minden fasza, akkor a dátum alapján ellenőrizni + foglalni kell terminált --> ezt még meg kell szülni, hogy hogyan legyen
        // TODO: 4. Ha OK a terminál, akkor le lehet foglalni a raktárhelyet is --> ezt még meg kell szülni, hogy hogyan legyen

        // Munkalap frissítése a szerveren
        if (checksPassed) {
            worksheet.setApproved();
            worksheets.put(worksheet.getTransactionID(),worksheet);
            // Munkalapok mentése fájlba
            Persistency.SaveObject(this.worksheets, "WorkSheets.ser");
            Persistency.SaveObject(this.workCounter, "WorkCounter.ser");
        }
        // TODO: 5. Ha valami szar, hibaüzit beállítani
        else {
            worksheet.setTransactionMessage(errorMessage);
        }
        return worksheet;
    }
}
