package com.berraktar;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class WorkOrders implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = 5107772686822488727L;
    // Munkalap számláló
    private static AtomicInteger workCounter;
    // Munkalapok
    private static ConcurrentHashMap<Integer, Worksheet> worksheets = new ConcurrentHashMap<>();

    // Kostruktor
    private WorkOrders(){
        loadWorkOrdersState();
    }

    // Munkalapok betöltése
    private static synchronized void loadWorkOrdersState(){
    // Mentett munkalapok betöltése
    if (new File("WorkSheets.ser").exists()) {
        worksheets = (ConcurrentHashMap<Integer, Worksheet>) Persistency.LoadObject("WorkSheets.ser");
    }

    // Mentett munkalap számláló betöltése
    if (new File("WorkCounter.ser").exists()) {
        workCounter = (AtomicInteger) Persistency.LoadObject("WorkCounter.ser");
    } else {
        workCounter = new AtomicInteger(0);
    }
}

    // Munkalapok mentése
    private static synchronized void saveWorkOrdersState() {
    // Adatok mentése fájlba
    Persistency.SaveObject(worksheets, "WorkSheets.ser");
    Persistency.SaveObject(workCounter, "WorkCounter.ser");
}

    // Új munkalap létrehozása
    public synchronized int CreateWorkSheet(Worksheet.WorkSheetType workSheetType) {
        Integer newID = workCounter.incrementAndGet();
        Worksheet newWorkSheet = new Worksheet(workSheetType);
        newWorkSheet.setTransactionID(newID);
        newWorkSheet.setInitialized();
        worksheets.put(newID, newWorkSheet);
        saveWorkOrdersState();
        return newID;
    }
}
