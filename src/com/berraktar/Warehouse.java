package com.berraktar;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        updateTerminalReservations();
    }

    // Adatok betöltése
    private void loadWarehouseState() {

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

    // Adatok mentése
    private synchronized void saveWarehouseState() {
        // Adatok mentése fájlba
        Persistency.SaveObject(this.cooledLocations, "CooledLocations.ser");
        Persistency.SaveObject(this.normalLocations, "NormalLocations.ser");
        Persistency.SaveObject(this.cooledTerminals, "CooledTerminals.ser");
        Persistency.SaveObject(this.normalTerminals, "NormalTerminals.ser");
    }

    // Terminál foglalások betöltése
    private synchronized void updateTerminalReservations() {
        Map<Integer, Worksheet> worklist = WorkOrders.getWorksheets();

        // Munkalapok adatainak kigyűjtése
        for (Map.Entry<Integer, Worksheet> entry : worklist.entrySet()) {
            Worksheet value = entry.getValue();
            if (value.isApproved() || value.isActive()) {
                if (value.isCooled()) {
                    if ( this.reservedCooledTerminals.get(value.getReservedDate()) == null ){
                        List<Integer> reserveList = new ArrayList<>();
                        this.reservedCooledTerminals.put(value.getReservedDate(), reserveList);
                        this.reservedCooledTerminals.get(value.getReservedDate()).add(value.getTerminalID());
                    } else {
                        this.reservedCooledTerminals.get(value.getReservedDate()).add(value.getTerminalID());
                    }
                } else {
                    if ( this.reservedNormalTerminals.get(value.getReservedDate()) == null ){
                        List<Integer> reserveList = new ArrayList<>();
                        this.reservedNormalTerminals.put(value.getReservedDate(), reserveList);
                        this.reservedNormalTerminals.get(value.getReservedDate()).add(value.getTerminalID());
                    } else {
                        this.reservedNormalTerminals.get(value.getReservedDate()).add(value.getTerminalID());
                    }
                }
            }
        }
    }

    // Beszállítás: Munkalap adatainak ellenőrzése, előfoglalás
    public synchronized ReservationMessage DoReservation(ReservationMessage reservationMessage, Accounting accounting) {
        Worksheet worksheet = WorkOrders.getWorksheet(reservationMessage.getTransactionID());
        Renter renter = accounting.getRenter(reservationMessage.getRenterID());

        // Elutasítás: bérlő nem létezik
        if (renter == null) {
            reservationMessage.setTransactionMessage("A megadott bérlő azonosító nem létezik: " + reservationMessage.getRenterID());
            worksheet.setRejected();
            return reservationMessage;
        }

        // Elutasítás: bérlőnek nincs elég szabad helye
        if (!renter.decreaseFreeSpace(reservationMessage.isCooled(), reservationMessage.getPallets())){
            reservationMessage.setTransactionMessage("A megadott bérlőnek nincs elég szabad helye: " + renter.getFreeLocations(reservationMessage.isCooled()));
            worksheet.setRejected();
            return reservationMessage;
        }

        // Elutasítás: elkésett a szállítmány
        if (reservationMessage.getReservationDate().isBefore(LocalDateTime.now())){
            reservationMessage.setTransactionMessage("A foglalás időpontja már elmúlt");
            worksheet.setRejected();
            return reservationMessage;
        }

        // Terminál szabad kapacitás ellenőrzése, előfoglalás
        int reservedTerminal = reserveTerminal(reservationMessage.isCooled(), reservationMessage.getReservationDate());
        if (reservedTerminal != 0) {
            worksheet.setTerminalID(reservedTerminal);
        } else {
            reservationMessage.setTransactionMessage("A megadott időpontban nincs szabad terminál");
            worksheet.setRejected();
            return reservationMessage;
        }

        // Raktár szabad kapacitásának ellenőrzése, előfoglalás
        List<Integer> reservedLocations = reserveLocations(reservationMessage.isCooled(), reservationMessage.getPallets(), reservationMessage.getRenterID());
        if (reservedLocations != null) {
            worksheet.setLocations(reservedLocations);
        } else {
            reservationMessage.setTransactionMessage("Nincs elég szabad lokáció a raktárban");
            worksheet.setRejected();
            return reservationMessage;
        }

        // Munkalap jóváhagyása, vagy elutasítása - TODO diszpécser még visszadobhatja

        // Munkalap jóváhagyása
        UserIO.fillWorkSheet(worksheet, reservationMessage);
        worksheet.setApproved();

        // Logisztikai művelet lejelentése
        accounting.addLogisticsOperations(renter.getCode(),1);

        // Állapot mentése
        saveWarehouseState();
        WorkOrders.saveWorkOrdersState();

        // Minden OK, mehet a visszaigazolás
        reservationMessage.setApproved();
        return reservationMessage;
    }

    // Beszállítás: Egy raklap kirakása a terminálra
    public synchronized UnloadingMessage DoUnloading(UnloadingMessage unloadingMessage) {
        Worksheet worksheet = WorkOrders.getWorksheet(unloadingMessage.getTransactionID());

        // Létezik a munkalap?
        if (worksheet == null) {
            unloadingMessage.setTransactionMessage("Ezen a számon nincs foglalás a rendszerben!");
            return unloadingMessage;
        }

        // Van még paletta a kocsin?
        Pallet pallet = worksheet.takePallet();
        if (pallet == null) {
            unloadingMessage.setTransactionMessage("Nincs több paletta a kocsin!");
            return unloadingMessage;
        }

        // Kipakolás a terminálra
        Terminal terminal;
        if (worksheet.isCooled()){
            terminal = this.cooledTerminals.get(worksheet.getTerminalID());
        } else {
            terminal = this.normalTerminals.get(worksheet.getTerminalID());
        }
        pallet.setInternalPartNumber(unloadingMessage.getInternalPartNumber());
        terminal.addPallet(pallet);
        terminal.setOccupied();

        // Állapot mentése
        saveWarehouseState();
        WorkOrders.saveWorkOrdersState();

        // Visszaigazolás
        unloadingMessage.setConfirmed();
        return unloadingMessage;
    }

    // Beszállítás: Raklapok berakása a lokációkba, munka készrejelentése
    public synchronized ReceivingMessage DoStoring(ReceivingMessage receivingMessage, Accounting accounting) {
        Worksheet worksheet = WorkOrders.getWorksheet(receivingMessage.getTransactionID());

        // Visszautasítás: tranzakció azonosító nem létezik
        if (worksheet == null) {
            receivingMessage.setTransactionMessage("Ezen a számon nincs foglalás a rendszerben!");
            return receivingMessage;
        }

        ConcurrentHashMap<Integer,Location> locations;
        if (worksheet.isCooled()){
            locations = this.cooledLocations;
        } else {
            locations = this.normalLocations;
        }

        Terminal terminal;
        if (worksheet.isCooled()){
            terminal = this.cooledTerminals.get(worksheet.getTerminalID());
        } else {
            terminal = this.normalTerminals.get(worksheet.getTerminalID());
        }

        Map<String, Pallet> palletList = terminal.getPalletList();
        List<Integer> reservedLocations = worksheet.getLocations();

        // Visszautasítás: palettaszám nem egyezik a foglalt lokációk számával
        if (palletList.size() != reservedLocations.size()) {
            receivingMessage.setTransactionMessage("Palettaszám nem egyezik a foglalt lokációk számával!");
            return receivingMessage;
        }

        // Kipakolás a lokációba
        for (int i = 0; i < reservedLocations.size(); i++){
            Pallet pallet = terminal.takePallet(receivingMessage.getInternalPartNumber());
            locations.get(reservedLocations.get(i)).addPallet(pallet);
        }

        // Visszaigazolás
        worksheet.setConfirmed();

        // Terminál felszabadítása
        terminal.setFree();

        // Logisztikai művelet lejelentése
        accounting.addLogisticsOperations(receivingMessage.getRenterID(),1);

        // Állapot mentése
        saveWarehouseState();
        WorkOrders.saveWorkOrdersState();

        receivingMessage.setCompleted();
        return receivingMessage;
    }

    // Visszamondás
    public synchronized ReceivingMessage CancelWorkSheet(ReceivingMessage receivingMessage) {
        Worksheet worksheet = WorkOrders.getWorksheet(receivingMessage.getTransactionID());
        // Renter renter = accounting.getRenter(reservationMessage.getRenterID());
        // TODO: Warehouse -> terminál felszabadítása
        // TODO: Renter -> lokációk felszabadítása
        // TODO: Accounting -> Logisztikai művelet lejelentése
        worksheet.setCancelled();
        saveWarehouseState();
        WorkOrders.saveWorkOrdersState();
        return receivingMessage;
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
            if (!locations.get(i).isReserved()){
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

    public synchronized int getFreeNormalLocation() {
        return this.freeNormalLocation;
    }
    public synchronized int getFreeCooledLocation() {
        return this.freeCooledLocation;
    }
    public synchronized int getFreeNormalTerminal() {
        return this.freeNormalTerminal;
    }
    public synchronized int getFreeCooledTerminal() {
        return this.freeCooledTerminal;
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

    public synchronized ConcurrentHashMap<Integer,Location> getNormalLocations(){
        return this.normalLocations;
    }
    public synchronized ConcurrentHashMap<Integer,Location> getCooledLocations(){
        return this.cooledLocations;
    }
    public synchronized ConcurrentHashMap<Integer,Terminal> getNormalTerminals(){
        return this.normalTerminals;
    }
    public synchronized ConcurrentHashMap<Integer,Terminal> getCooledTerminals(){
        return this.cooledTerminals;
    }

    public synchronized ConcurrentHashMap<LocalDateTime,List<Integer>> getReservedNormalTerminals(){
        return this.reservedNormalTerminals;
    }
    public synchronized ConcurrentHashMap<LocalDateTime,List<Integer>> getReservedCooledTerminals(){
        return this.reservedCooledTerminals;
    }

}
