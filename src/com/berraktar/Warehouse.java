package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Warehouse implements Serializable {
    private final long serialVersionUID = 1041441426752225702L;
    private final int maxNormalLocation = 3000;   // Normál lokációk maximális száma
    private final int maxCooledLocation = 800;   // Hűtött lokációk maximális száma
    private final int maxNormalTerminal = 9;   // Normál terminálok maximális száma
    private final int maxCooledTerminal = 3;   // Hűtött terminálok maximális száma
    private ConcurrentHashMap<Integer,Location> normalLocations = new ConcurrentHashMap<>();   // Normál lokáció objektumok
    private ConcurrentHashMap<Integer,Location> cooledLocations = new ConcurrentHashMap<>();   // Hűtött lokáció objektumok
    private ConcurrentHashMap<Integer,Terminal> normalTerminals = new ConcurrentHashMap<>();   // Normál terminál objektumok
    private ConcurrentHashMap<Integer,Terminal> cooledTerminals = new ConcurrentHashMap<>();   // Hűtött terminál objektumok
    private ConcurrentHashMap<Integer, Worksheet> worksheets = new ConcurrentHashMap<>();       // Munkalapok
    private AtomicInteger workCounter;

    public Warehouse(){
        // TODO: induláskor adatokat betölteni
        workCounter = new AtomicInteger(0);
    }

    // TODO: Meg tudod nézni, van-e annyi szabad terminál
    public Terminal ReserveTerminal(LocalDateTime date, boolean isCooled){
        if (isCooled) {

        }
        else {

        }


        return null;
    }

    public Worksheet CreateWorkSheet(Worksheet.WorkType workType) {
        Integer newID = workCounter.incrementAndGet();
        Worksheet newWorkSheet = new Worksheet(workType);
        newWorkSheet.setTransactionID(newID);
        newWorkSheet.setInitialized();
        worksheets.put(newID, newWorkSheet);
        return newWorkSheet;
    }
}
