package com.berraktar;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Warehouse implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = 1041441426752225702L;

    // Raktár tulajdonságai
    private final int maxNormalLocation; // Normál lokációk maximális száma
    private final int maxCooledLocation; // Hűtött lokációk maximális száma
    private final int maxNormalTerminal; // Normál terminálok maximális száma
    private final int maxCooledTerminal; // Hűtött terminálok maximális száma

    private int freeNormalLocation; // Szabad normál lokációk száma
    private int freeCooledLocation; // Szabad hűtött lokációk száma
    private int freeNormalTerminal; // Szabad normál terminálok száma
    private int freeCooledTerminal; // Szabad hűtött terminálok száma

    // Raktározási adatok
    private ConcurrentHashMap<Integer,Location> normalLocations = new ConcurrentHashMap<>(); // Normál lokáció objektumok
    private ConcurrentHashMap<Integer,Location> cooledLocations = new ConcurrentHashMap<>(); // Hűtött lokáció objektumok
    private ConcurrentHashMap<Integer,Terminal> normalTerminals = new ConcurrentHashMap<>(); // Normál terminál objektumok
    private ConcurrentHashMap<Integer,Terminal> cooledTerminals = new ConcurrentHashMap<>(); // Hűtött terminál objektumok

    // Tranzakció adatok
    private ConcurrentHashMap<LocalDateTime,List<Integer>> reservedNormalTerminals = new ConcurrentHashMap<>(); // Foglalt normál terminálok dátum szerint
    private ConcurrentHashMap<LocalDateTime,List<Integer>> reservedCooledTerminals = new ConcurrentHashMap<>(); // Foglalt hűtött terminálok dátum szerint
    private ConcurrentHashMap<Integer, Worksheet> worksheets = new ConcurrentHashMap<>();                       // Munkalapok
    private AtomicInteger workCounter;

    // Konstruktor
    public Warehouse(int maxNormalLocation, int maxCooledLocation, int maxNormalTerminal, int maxCooledTerminal){
        this.maxNormalLocation = maxNormalLocation;
        this.maxCooledLocation = maxCooledLocation;
        this.maxNormalTerminal = maxNormalTerminal;
        this.maxCooledTerminal = maxCooledTerminal;
        this.freeCooledLocation = maxNormalLocation;
        this.freeCooledLocation = maxCooledLocation;
        this.freeCooledLocation = maxNormalTerminal;
        this.freeCooledLocation = maxCooledTerminal;

        // Mentett munkalapok betöltése
        if (new File("WorkSheets.ser").exists()) {
            this.worksheets = (ConcurrentHashMap<Integer, Worksheet>) Persistency.LoadObject("WorkSheets.ser");
        }

        // Mentett munkalap számláló betöltése
        if (new File("WorkCounter.ser").exists()) {
            this.workCounter = (AtomicInteger) Persistency.LoadObject("WorkCounter.ser");
        } else {
            workCounter = new AtomicInteger(0);
        }
    }

    // Új munkalap létrehozása
    public synchronized Worksheet CreateWorkSheet(Worksheet.WorkType workType) {
        Integer newID = workCounter.incrementAndGet();
        Worksheet newWorkSheet = new Worksheet(workType);
        newWorkSheet.setTransactionID(newID);
        newWorkSheet.setInitialized();
        worksheets.put(newID, newWorkSheet);
        return newWorkSheet;
    }

    // Munkalap adatainak ellenőrzése, előfoglalás
    public synchronized Worksheet ApproveWorkSheet(Worksheet worksheet, Accounting accounting) {

        // Bérlő létezésének ellenőrzése
        Renter renter = accounting.getRenter(worksheet.getRenterID());
        if (renter == null) {
            worksheet.setTransactionMessage("A megadott bérlő azonosító nem létezik: " + worksheet.getRenterID());
            return worksheet;
        }

        // Bérlő szabad kapacitásának ellenőrzése, előfoglalás
        if (!renter.decreaseFreeSpace(worksheet.isCooled(),worksheet.getNumberOfPallets())){
            worksheet.setTransactionMessage("A megadott bérlőnek nincs elég szabad helye: " + renter.getFreeLocations(worksheet.isCooled()));
            return worksheet;
        }

        // Dátum ellenőrzése
        if (worksheet.getReservedDate().isBefore(LocalDateTime.now())){
            worksheet.setTransactionMessage("A foglalás időpontja már elmúlt");
            return worksheet;
        }

        // Terminál szabad kapacitás ellenőrzése, előfoglalás
        int reservedTerminal = reserveTerminal(worksheet.isCooled(), worksheet.getReservedDate());
        if (reservedTerminal != 0) {
            worksheet.setTerminalID(reservedTerminal);
        } else {
            worksheet.setTransactionMessage("A megadott időpontban nincs szabad terminál");
            return worksheet;
        }

        // Raktár szabad kapacitásának ellenőrzése, előfoglalás
        List<Integer> reservedLocations = reserveLocations(worksheet.isCooled(), worksheet.getNumberOfPallets());
        if (reservedLocations != null) {
            worksheet.setLocations(reservedLocations);
        } else {
            worksheet.setTransactionMessage("Nincs elég szabad lokáció a raktárban");
            return worksheet;
        }

        // Munkalap jóváhagyása, vagy elutasítása - TODO diszpécser még visszadobhatja
        worksheet.setApproved();
        worksheets.put(worksheet.getTransactionID(),worksheet);

        // Logisztikai művelet lejelentése
        accounting.getRenter(renter.getCode()).addLogisticsOperations(1);

        // Adatok mentése fájlba
        Persistency.SaveObject(accounting.getRenters(), "Renters.ser");
        Persistency.SaveObject(this.worksheets, "WorkSheets.ser");
        Persistency.SaveObject(this.workCounter, "WorkCounter.ser");
        return worksheet;
    }

    // Munkalap aktiválása
    // TODO: Beérkezési dátum paramétert hozzá kell adni
    public synchronized Worksheet ActivateWorkSheet(Worksheet worksheet) {
        // TODO: ellenőrizni, hogy a beérkezés időpontja megfelel-e a foglalásnak, ha igen, aktiválni a munkalapot, ha nem, hibaüzenetet beállítani
        return worksheet;
    }

    // Munkalap lezárása (végrehajtva)
    public synchronized Worksheet ConfirmWorkSheet(Worksheet worksheet) {
        // TODO: Warehouse -> terminál felszabadítása
        // TODO: Renter -> lokációk felszabadítása
        // TODO: Accounting -> Logisztikai művelet lejelentése
        worksheet.setConfirmed();
        return worksheet;
    }

    public synchronized Worksheet CancelWorkSheet(Worksheet worksheet) {
        // TODO: Warehouse -> terminál felszabadítása
        // TODO: Renter -> lokációk felszabadítása
        // TODO: Accounting -> Logisztikai művelet lejelentése
        worksheet.setCancelled();
        return worksheet;
    }

    // Lokáció foglalás
    private synchronized List<Integer> reserveLocations(boolean isCooled, int numberOfPallets) {
        List<Integer> locationList = new ArrayList<>();
        ConcurrentHashMap<Integer, Location> locations;

        // Hűtött lokáció
        if (isCooled) {
            locations = this.cooledLocations;
        }
        // Normál lokáció
        else {
            locations = this.normalLocations;
        }

        // Szabad lokációk összegyűjtése
        while (locationList.size() != numberOfPallets){
            int nextFreeLocation;
            nextFreeLocation = getNextFreeLocation(isCooled);
            System.out.println(nextFreeLocation);
            if (nextFreeLocation != 0){
                locations.put(nextFreeLocation, new Location(nextFreeLocation));
                locationList.add(nextFreeLocation);
            } else {
                return null;
            }
        }
        return locationList;
    }

    // Szabad lokáció keresés
    private synchronized int getNextFreeLocation(boolean isCooled){
        ConcurrentHashMap<Integer,Location> locations;
        int maxLocations;
        if(isCooled){
            locations = this.cooledLocations;
            maxLocations = this.getMaxCooledLocation();
        } else {
            locations = this.normalLocations;
            maxLocations = this.getMaxNormalLocation();
        }

        for (int i = 1; i < maxLocations; i++) {
            if (locations.get(i) == null){
                return i;
            }
        }
        return 0;
    }

    // Terminál foglalás
    private synchronized int reserveTerminal(boolean isCooled, LocalDateTime reserveDate){
        List<Integer> terminalList;
        int maxTerminals;

        // Hűtött terminál
        if (isCooled){
            terminalList = this.reservedCooledTerminals.get(reserveDate);
            maxTerminals = this.getMaxCooledTerminal();
        }
        // Normál terminál
        else {
            terminalList = this.reservedNormalTerminals.get(reserveDate);
            maxTerminals = this.getMaxNormalTerminal();
        }
        // Van-e szabad terminál
        int listSize;
        // Ha a megadott dátum alatt létezik már terminállista, akkor hozzá kell adni az új foglalást
        if (terminalList != null) {
            listSize = this.reservedNormalTerminals.get(reserveDate).size();
            if (listSize < maxTerminals) {
                // Első szabad terminál sorszámának keresése és visszaadása
                for (int i = 1; i < maxTerminals; i++){
                    if (!terminalList.contains(i)){
                        terminalList.add(i);
                        return i;
                    }
                }
            }
        }
        // Ha a megadott dátum alatt még nem létezik terminállista, akkor létre kell hozni a listát
        else {
            List<Integer> reserveList = new ArrayList<>();
            reserveList.add(1);
            if (isCooled){
                this.reservedCooledTerminals.put(reserveDate,reserveList);
            } else {
                this.reservedNormalTerminals.put(reserveDate,reserveList);
            }
            return 1;
        }
        // Sikertelen foglalás
        return 0;
    }

    // Jelentések

    // TODO: bérlők jelentés
    public synchronized Report RenterReport(Report report) {
        StringBuilder reply = new StringBuilder();

        reply.append("Report nincs lefejlesztve!");

        report.setReply(reply.toString());
        return report;
    }

    // TODO: munkalapok jelentés
    public synchronized Report WorksheetReport(Report report) {
        Map<Integer, Worksheet> worklist = this.worksheets;
        StringBuilder reply = new StringBuilder();
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm");
        reply.append("\nID#\t\tRenter\tDate\t\t\tTime\tPallets\tWork\tTerminal\tLocations");

        for (Map.Entry<Integer, Worksheet> entry : worklist.entrySet()) {
            Worksheet value = entry.getValue();
            reply.append("\n" + entry.getKey() + "\t\t");
            reply.append(value.getRenterID() + "\t");
            reply.append(value.getReservedDate().format(date) + "\t\t");
            reply.append(value.getReservedDate().format(time) + "\t");
            reply.append(value.getNumberOfPallets() + "\t\t");
            reply.append(value.getWorkType() + "\t");
            reply.append(value.getTerminalID() + "\t\t");
            reply.append(value.getLocations() + "\t");
        }
        report.setReply(reply.toString());
        return report;
    }

    // TODO: lokációk jelentés
    public synchronized Report LocationReport(Report report) {
        StringBuilder reply = new StringBuilder();

        reply.append("Report nincs lefejlesztve!");

        report.setReply(reply.toString());
        return report;
    }

    // Getterek, Setterek

    public int getMaxNormalLocation() {
        return maxNormalLocation;
    }

    public int getMaxCooledLocation() {
        return maxCooledLocation;
    }

    public int getMaxNormalTerminal() {
        return maxNormalLocation;
    }

    public int getMaxCooledTerminal() {
        return maxCooledLocation;
    }

    public void increaseFreeCooledLocations(int byAmount){
        this.freeCooledLocation += byAmount;
    }

    public void increaseFreeNormalLocations(int byAmount){
        this.freeNormalLocation += byAmount;
    }

    public void decreaseFreeCooledLocations(int byAmount){
        this.freeCooledLocation -= byAmount;
    }

    public void decreaseFreeNormalLocations(int byAmount){
        this.freeNormalLocation -= byAmount;
    }

}
