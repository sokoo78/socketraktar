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
    Warehouse(int maxNormalLocation, int maxCooledLocation, int maxNormalTerminal, int maxCooledTerminal){
        this.maxNormalLocation = maxNormalLocation;
        this.maxCooledLocation = maxCooledLocation;
        this.maxNormalTerminal = maxNormalTerminal;
        this.maxCooledTerminal = maxCooledTerminal;
        this.freeNormalLocation = this.maxNormalLocation;
        this.freeCooledLocation = this.maxCooledLocation;
        this.freeNormalTerminal = this.maxNormalTerminal;
        this.freeCooledTerminal = this.maxCooledTerminal;
        loadWarehouseState();
    }

    // Adatok betöltése
    private void loadWarehouseState() {
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

        // Mentett normál terminálok betöltése
        if (new File("NormalTerminals.ser").exists()) {
            this.normalTerminals = (ConcurrentHashMap<Integer,Terminal>) Persistency.LoadObject("NormalTerminals.ser");
        } else {
            for (int i = 1; i < this.maxNormalTerminal; i++){
                this.normalTerminals.put(i,new Terminal(i));
            }
        }

        // Mentett hűtött terminálok betöltése
        if (new File("CooledTerminals.ser").exists()) {
            this.cooledTerminals = (ConcurrentHashMap<Integer,Terminal>) Persistency.LoadObject("CooledTerminals.ser");
        } else {
            for (int i = 1; i < this.maxCooledTerminal; i++){
                this.cooledTerminals.put(i,new Terminal(i));
            }
        }

        // Mentett normál lokációk betöltése
        if (new File("NormalLocations.ser").exists()) {
            this.normalLocations = (ConcurrentHashMap<Integer,Location>) Persistency.LoadObject("NormalLocations.ser");
        } else {
            for (int i = 1; i < this.maxNormalLocation; i++){
                this.normalLocations.put(i,new Location(i,null, false));
            }
        }

        // Mentett hűtött lokációk betöltése
        if (new File("CooledLocations.ser").exists()) {
            this.cooledLocations = (ConcurrentHashMap<Integer,Location>) Persistency.LoadObject("CooledLocations.ser");
        } else {
            for (int i = 1; i < this.maxCooledLocation; i++){
                this.cooledLocations.put(i,new Location(i,null, false));
            }
        }
    }

    private synchronized void saveWarehouseState() {
        // Adatok mentése fájlba
        Persistency.SaveObject(this.worksheets, "WorkSheets.ser");
        Persistency.SaveObject(this.workCounter, "WorkCounter.ser");
        Persistency.SaveObject(this.cooledLocations, "CooledLocations.ser");
        Persistency.SaveObject(this.normalLocations, "NormalLocations.ser");
    }

    // Új munkalap létrehozása
    public synchronized int CreateWorkSheet(Worksheet.WorkSheetType workSheetType) {
        Integer newID = workCounter.incrementAndGet();
        Worksheet newWorkSheet = new Worksheet(workSheetType);
        newWorkSheet.setTransactionID(newID);
        newWorkSheet.setInitialized();
        worksheets.put(newID, newWorkSheet);
        return newID;
    }

    // Munkalap adatainak ellenőrzése, előfoglalás
    public synchronized Reservation ApproveWorkSheet(Reservation reservation, Accounting accounting) {

        // Bérlő létezésének ellenőrzése
        Renter renter = accounting.getRenter(reservation.getRenterID());
        if (renter == null) {
            reservation.setTransactionMessage("A megadott bérlő azonosító nem létezik: " + reservation.getRenterID());
            return reservation;
        }

        // Bérlő szabad kapacitásának ellenőrzése, előfoglalás
        if (!renter.decreaseFreeSpace(reservation.isCooled(),reservation.getPallets())){
            reservation.setTransactionMessage("A megadott bérlőnek nincs elég szabad helye: " + renter.getFreeLocations(reservation.isCooled()));
            return reservation;
        }

        // Dátum ellenőrzése
        if (reservation.getReservationDate().isBefore(LocalDateTime.now())){
            reservation.setTransactionMessage("A foglalás időpontja már elmúlt");
            return reservation;
        }

        Worksheet worksheet = this.worksheets.get(reservation.getTransactionID());

        // Terminál szabad kapacitás ellenőrzése, előfoglalás
        int reservedTerminal = reserveTerminal(reservation.isCooled(), reservation.getReservationDate());
        if (reservedTerminal != 0) {
            worksheet.setTerminalID(reservedTerminal);
        } else {
            reservation.setTransactionMessage("A megadott időpontban nincs szabad terminál");
            return reservation;
        }

        // Raktár szabad kapacitásának ellenőrzése, előfoglalás
        List<Integer> reservedLocations = reserveLocations(reservation.isCooled(), reservation.getPallets(), reservation.getRenterID());
        if (reservedLocations != null) {
            worksheet.setLocations(reservedLocations);
        } else {
            reservation.setTransactionMessage("Nincs elég szabad lokáció a raktárban");
            return reservation;
        }

        // Munkalap jóváhagyása, vagy elutasítása - TODO diszpécser még visszadobhatja

        // Munkalap jóváhagyása
        reservation.setApproved();
        UserIO.fillWorkSheet(worksheet,reservation);
        worksheet.setApproved();

        // Logisztikai művelet lejelentése
        accounting.addLogisticsOperations(renter.getCode(),1);

        // Állapot mentése
        saveWarehouseState();

        // Minden OK, mehet a visszaigazolás
        return reservation;
    }

    // Munkalap aktiválása
    public synchronized Receiving ActivateWorkSheet(Receiving receiving) {
        Worksheet worksheet = this.worksheets.get(receiving.getTransactionID());

        // Létezik-e a munkalap
        if (worksheet == null){
            receiving.setTransactionMessage("Ezen a számon nincs foglalás a rendszerben!");
            return receiving;
        }

        // Nincs-e már aktiválva
        if (worksheet.isActive()) {
            receiving.setTransactionMessage("Ez a munkalap már aktív!");
            return receiving;
        }

        // Megfelelő-e a beérkezés dátuma
        if (UserIO.DateisInRange(worksheet.getReservedDate(), worksheet.getReservedDate().plusMinutes(30),receiving.getReceivingDate())) {
            worksheet.setActive();
            receiving.setApproved();
            return receiving;
        } else {
            receiving.setTransactionMessage("Nem megfelelő érkezési dátum: " +
                    UserIO.printDate(worksheet.getReservedDate()) + "-" +
                    worksheet.getReservedDate().getHour() + ":" + worksheet.getReservedDate().plusMinutes(30).getMinute() +
                    " helyett " + UserIO.printDate(receiving.getReceivingDate()));
            return receiving;
        }
    }

    public Receiving ProcessWorkSheet(Receiving receiving) {
        // TODO: Bérlő, terminál, raklapok, lokációk adatokat összeszedni
        return receiving;
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
    private synchronized List<Integer> reserveLocations(boolean isCooled, int numberOfPallets, String renterID) {
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
        while (locationList.size() != numberOfPallets){         // Amíg nincs meg a szükséges mennyiségű paletta
            int nextFreeLocation;
            nextFreeLocation = getNextFreeLocation(isCooled);   // Szabad lokáció keresése
            if (nextFreeLocation != 0){                         // Ha talált a kereső szabad lokációt, mehet a listába
                locations.put(nextFreeLocation, new Location(nextFreeLocation, renterID, true));
                locationList.add(nextFreeLocation);
            } else {                                            // Ha nem talált, és még nincs elég paletta hely
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
            if (locations.get(i).isReserved() == false){
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
        // Ha a megadott dátum alatt létezik már terminállista, akkor hozzá kell adni az új foglalást
        if (terminalList != null) {
            if (terminalList.size() < maxTerminals) {   // Nem foglalt az összes terminál
                for (int i = 1; i <= maxTerminals; i++){ // Első szabad terminál sorszámának keresése és visszaadása (1-es ID van a 0. helyen!)
                    if (!terminalList.contains(i)){
                        terminalList.add(i);
                        return i;
                    }
                }
            } else {
                return 0; // Minden terminál foglalt az adott időpontban
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

    // Reports

    // TODO: bérlők jelentés
    public synchronized Report RenterReport(Report report) {
        StringBuilder reply = new StringBuilder();

        reply.append("Report nincs lefejlesztve!");

        report.setReply(reply.toString());
        return report;
    }

    // Munkalapok jelentés
    public synchronized Report WorksheetReport(Report report) {
        Map<Integer, Worksheet> worklist = this.worksheets;
        StringBuilder reply = new StringBuilder();
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm");

        // Fejléc
        reply.append("\nID#\t\tStátusz\t\tBérlő#\tDátum\t\tIdő\t\tPaletta#\tMunka\tTerminál\tLokációk");

        // Munkalapok adatainak kigyűjtése
        for (Map.Entry<Integer, Worksheet> entry : worklist.entrySet()) {
            Worksheet value = entry.getValue();
            reply.append("\n").append(entry.getKey()).append("\t\t");
            reply.append(value.getStatus()).append("\t");
            if (value.isApproved()) {
                reply.append(value.getRenterID()).append("\t");
                reply.append(value.getReservedDate().format(date)).append("\t");
                reply.append(value.getReservedDate().format(time)).append("\t");
                reply.append(value.getNumberOfPallets()).append("\t\t\t");
                reply.append(value.getWorkSheetType()).append("\t");
                reply.append(value.getTerminalID()).append("\t\t");
                if (value.isCooled()){
                    reply.append(" (Hűtött) ");
                } else {
                    reply.append(" (Normál) ");
                }
                reply.append(value.getLocations());
            }
        }

        // Válasz mentése a jelentésbe
        report.setReply(reply.toString());
        return report;
    }

    // Lokáció jelentés
    public synchronized Report LocationReport(Report report) {
        StringBuilder reply = new StringBuilder();
        Map<Integer,Location> normalLocations = this.normalLocations;
        Map<Integer,Location> cooledLocations = this.cooledLocations;

        // Fejléc
        reply.append("\nLokáció összefoglaló\n\n");

        // Lokáció fő adatok
        reply.append("Szabad / maximális normál lokációk száma:   \t");
        reply.append(this.freeNormalLocation).append(" / ").append(this.getMaxNormalLocation()).append("\n");
        reply.append("Szabad / maximális hűtött lokációk száma:   \t");
        reply.append(this.freeCooledLocation).append(" / ").append(this.getMaxCooledLocation()).append("\n");

        // Lokáció részletek
        reply.append("\nFoglalt normál lokációk listája\n");
        reply.append("\nLocID#\tRenterID\tInternalID\tExternalID");
        getLocationList(reply, normalLocations);

        reply.append("\n\nFoglalt hűtött lokációk listája\n");
        reply.append("\nLocID#\tRenterID\tInternalID\tExternalID");
        getLocationList(reply, cooledLocations);

        // Válasz mentése a jelentésbe
        report.setReply(reply.toString());
        return report;
    }

    // Lokáció lista lokáció jelentéshez
    private synchronized void getLocationList(StringBuilder reply, Map<Integer, Location> locations) {
        for (Map.Entry<Integer, Location> entry : locations.entrySet()) {
            Location value = entry.getValue();
            if (value.getRenterID() != null) {
                reply.append("\n").append(entry.getKey()).append("\t\t");
                reply.append(value.getRenterID()).append("\t\t\t");
                reply.append(value.scanPalletInternalID()).append("\t\t\t");
                reply.append(value.scanPalletExternalID()).append("\t\t\t");
            }
        }
    }

    // Terminál foglalások jelentés - TODO ez még valamiért nem müxik
    public synchronized Report TerminalReport(Report report) {
        StringBuilder reply = new StringBuilder();

        // Fejléc
        reply.append("\nTerminál foglalások\n\n");

        // Terminál fő adatok
        reply.append("Szabad / maximális normál terminálok száma: \t");
        reply.append(this.freeNormalTerminal).append(" / ").append(this.getMaxNormalTerminal()).append("\n");
        reply.append("Szabad / maximális hűtött terminálok száma: \t");
        reply.append(this.freeCooledTerminal).append(" / ").append(this.getMaxCooledTerminal()).append("\n");

        // Terminál részletek
        reply.append("\nNormál terminál foglalások listája\n");
        reply.append("\nDátum\t\t\tTerminálok");
        getTerminalList(reply, reservedNormalTerminals);

        reply.append("\n\nHűtött terminál foglalások listája\n");
        reply.append("\nDátum\t\t\tTerminálok");
        getTerminalList(reply, reservedCooledTerminals);

        // Válasz mentése a jelentésbe
        report.setReply(reply.toString());
        return report;
    }

    private void getTerminalList(StringBuilder reply, ConcurrentHashMap<LocalDateTime, List<Integer>> terminals) {
        for (Map.Entry<LocalDateTime, List<Integer>> entry : terminals.entrySet()) {
            reply.append("\n").append(entry.getKey()).append("\t\t").append(entry.getValue());
            System.out.println(reply);
        }
    }

    // Getterek, Setterek

    public synchronized int getMaxNormalLocation() {
        return this.maxNormalLocation;
    }

    public synchronized int getMaxCooledLocation() {
        return this.maxCooledLocation;
    }

    public synchronized int getMaxNormalTerminal() {
        return this.maxNormalTerminal;
    }

    public synchronized int getMaxCooledTerminal() {
        return this.maxCooledTerminal;
    }

    public synchronized void increaseFreeCooledLocations(int byAmount){
        this.freeCooledLocation += byAmount;
    }

    public synchronized void increaseFreeNormalLocations(int byAmount){
        this.freeNormalLocation += byAmount;
    }

    public synchronized void decreaseFreeCooledLocations(int byAmount){
        this.freeCooledLocation -= byAmount;
    }

    public synchronized void decreaseFreeNormalLocations(int byAmount){
        this.freeNormalLocation -= byAmount;
    }

}
