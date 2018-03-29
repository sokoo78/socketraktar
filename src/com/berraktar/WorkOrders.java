package com.berraktar;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class WorkOrders {

    // Munkalap számláló
    private static AtomicInteger workCounter;
    // Munkalapok
    private static ConcurrentHashMap<Integer, Worksheet> worksheets = new ConcurrentHashMap<>();

    // Kostruktor
    static {
        loadWorkOrdersState();
    }

    // Munkalapok betöltése
    private static synchronized void loadWorkOrdersState() {
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
    static synchronized void saveWorkOrdersState() {
    Persistency.SaveObject(worksheets, "WorkSheets.ser");
    Persistency.SaveObject(workCounter, "WorkCounter.ser");
}

    // Új munkalap létrehozása
    static synchronized int CreateWorkSheet(MessageCreate messageCreate) {
        Integer newID = workCounter.incrementAndGet();
        Worksheet worksheet;
        if (messageCreate.isIncoming()){
            worksheet = new Worksheet(Worksheet.WorkSheetType.Incoming);
        } else {
            worksheet = new Worksheet(Worksheet.WorkSheetType.Outgoing);
        }
        worksheet.setTransactionID(newID);
        worksheet.setInitialized();
        worksheets.put(newID, worksheet);
        saveWorkOrdersState();
        return newID;
    }

    // Munkalap aktiválása
    static synchronized MessageActivate ActivateWorkSheet(MessageActivate messageActivate) {
        Worksheet worksheet = worksheets.get(messageActivate.getTransactionID());

        // Elutasítás: Nem létezik a munkalap
        if (worksheet == null){
            messageActivate.setTransactionMessage("Ezen a számon nincs foglalás a rendszerben!");
            return messageActivate;
        }

        // Elutasítás: Már aktiválva van
        if (worksheet.isActive()) {
            messageActivate.setTransactionMessage("Ez a munkalap már aktív!");
            return messageActivate;
        }

        // Elutasítás: nem megfelelő érkezési dátum
        if (!UserIO.DateisInRange(worksheet.getReservedDate(), worksheet.getReservedDate().plusMinutes(30), messageActivate.getReceivingDate())) {
            messageActivate.setTransactionMessage("Nem megfelelő érkezési dátum: " +
                    UserIO.printDate(worksheet.getReservedDate()) + "-" +
                    worksheet.getReservedDate().getHour() + ":" + worksheet.getReservedDate().plusMinutes(30).getMinute() +
                    " helyett " + UserIO.printDate(messageActivate.getReceivingDate()));
            return messageActivate;
        }

        // Jóváhagyás
        worksheet.setActive();

        // Állapot mentése
        WorkOrders.saveWorkOrdersState();

        // Visszaigazolás
        messageActivate.setApproved();
        return messageActivate;
    }

    // Munkalap végrehajtásának indítása
    static synchronized MessageProcess ProcessWorkSheet(MessageProcess messageProcess) {
        Worksheet worksheet = worksheets.get(messageProcess.getTransactionID());

        // Létezik a munkalap és megfelelő státuszban van?
        if (worksheet == null || !worksheet.isActive()) {
            messageProcess.setTransactionMessage("Ezen a számon nincs munkalap a rendszerben!");
            return messageProcess;
        }

        // Kiszállításhoz szükséges adatok kitöltése
        if (worksheet.getWorkSheetType() == Worksheet.WorkSheetType.Outgoing) {
            messageProcess.setLocations(worksheet.getLocations());
        }

        // Beérkeztetéshez és kiszállításhoz is szükséges adatok kitöltése
        messageProcess.setPallets(worksheet.getNumberOfPallets());
        messageProcess.setRenterID(worksheet.getRenterID());
        messageProcess.setExternalPartNumber(worksheet.getExternalPartNumber());
        messageProcess.setTerminalID(worksheet.getTerminalID());

        // Jóváhagyás
        worksheet.setProcessing();

        // Állapot mentése
        WorkOrders.saveWorkOrdersState();

        // Visszaigazolás
        messageProcess.setApproved();
        return messageProcess;
    }

    // Getterek, setterek

    static synchronized ConcurrentHashMap<Integer, Worksheet> getWorksheets() {
        return worksheets;
    }

    static synchronized Worksheet getWorksheet(int transactionID) {
        return worksheets.get(transactionID);
    }
}
