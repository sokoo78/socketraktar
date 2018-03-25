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
    public static synchronized void saveWorkOrdersState() {
    // Adatok mentése fájlba
    Persistency.SaveObject(worksheets, "WorkSheets.ser");
    Persistency.SaveObject(workCounter, "WorkCounter.ser");
}

    // Új munkalap létrehozása
    public static synchronized int CreateWorkSheet(MessageCreate messageCreate) {
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
    public static synchronized MessageActivate ActivateWorkSheet(MessageActivate messageActivate) {
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
    public static synchronized MessageProcess ProcessWorkSheet(MessageProcess messageProcess) {
        Worksheet worksheet = worksheets.get(messageProcess.getTransactionID());

        // Létezik a munkalap?
        if (worksheet == null) {
            messageProcess.setTransactionMessage("Ezen a számon nincs foglalás a rendszerben!");
            return messageProcess;
        }

        // Beérkeztetéshez szükséges adatok kitöltése
        messageProcess.setRenterID(worksheet.getRenterID());
        messageProcess.setPallets(worksheet.getNumberOfPallets());
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

    public static synchronized ConcurrentHashMap<Integer, Worksheet> getWorksheets() {
        return worksheets;
    }

    public static synchronized Worksheet getWorksheet(int transactionID) {
        return worksheets.get(transactionID);
    }
}
