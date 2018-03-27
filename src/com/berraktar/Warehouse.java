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

    // Beszállítás: Adatok ellenőrzése, előfoglalás
    synchronized MessageReserve DoReservation(MessageReserve messageReserve, Accounting accounting) {
        Worksheet worksheet = WorkOrders.getWorksheet(messageReserve.getTransactionID());
        Renter renter = accounting.getRenter(messageReserve.getRenterID());

        // Elutasítás: bérlő nem létezik
        if (renter == null) {
            messageReserve.setTransactionMessage("A megadott bérlő azonosító nem létezik: " + messageReserve.getRenterID());
            worksheet.setRejected();
            return messageReserve;
        }

        // Elutasítás: bérlőnek nincs elég szabad helye
        if (!renter.decreaseFreeSpace(messageReserve.isCooled(), messageReserve.getPallets())){
            messageReserve.setTransactionMessage("A megadott bérlőnek nincs elég szabad helye: " + renter.getFreeLocations(messageReserve.isCooled()));
            worksheet.setRejected();
            return messageReserve;
        }

        // Elutasítás: foglalás időpontja már elmúlt
        if (messageReserve.getReservationDate().isBefore(LocalDateTime.now())){
            messageReserve.setTransactionMessage("A foglalás időpontja már elmúlt");
            worksheet.setRejected();
            return messageReserve;
        }

        // Terminál szabad kapacitás ellenőrzése, előfoglalás
        int reservedTerminal = reserveTerminal(messageReserve.isCooled(), messageReserve.getReservationDate());
        if (reservedTerminal != 0) {
            worksheet.setTerminalID(reservedTerminal);
        } else {
            messageReserve.setTransactionMessage("A megadott időpontban nincs szabad terminál");
            worksheet.setRejected();
            return messageReserve;
        }

        // Raktár szabad kapacitásának ellenőrzése, előfoglalás
        List<Integer> reservedLocations = reserveLocations(messageReserve.isCooled(), messageReserve.getPallets(), messageReserve.getRenterID());
        if (reservedLocations != null) {
            worksheet.setLocations(reservedLocations);
        } else {
            messageReserve.setTransactionMessage("Nincs elég szabad lokáció a raktárban");
            worksheet.setRejected();
            return messageReserve;
        }

        // Munkalap jóváhagyása, vagy elutasítása - TODO diszpécser még visszadobhatja

        // Munkalap jóváhagyása
        UserIO.fillWorkSheet(worksheet, messageReserve);
        worksheet.setApproved();

        // Logisztikai művelet lejelentése
        accounting.addLogisticsOperations(renter.getCode(),1);

        // Állapot mentése
        saveWarehouseState();
        WorkOrders.saveWorkOrdersState();

        // Minden OK, mehet a visszaigazolás
        messageReserve.setApproved();
        return messageReserve;
    }

    // Kiszállítás: Adatok ellenőrzése, előfoglalás
    MessageOrder DoOrder(MessageOrder messageOrder, Accounting accounting) {
        Worksheet worksheet = WorkOrders.getWorksheet(messageOrder.getTransactionID());
        Renter renter = accounting.getRenter(messageOrder.getRenterID());

        // Elutasítás: bérlő nem létezik
        if (renter == null) {
            messageOrder.setTransactionMessage("A megadott bérlő azonosító nem létezik: " + messageOrder.getRenterID());
            worksheet.setRejected();
            return messageOrder;
        }

        // Palettaszám ellenőrzése, beállítása a munkalapon + lokációk kitöltése
        List<Integer> locationList;
        // Normál lokációk lekérdezése
        locationList = getLocationListByPartNumber(messageOrder.getPartNumber(), messageOrder.getPallets(), false);
        // Hűtött lokációk lekérdezése, ha a normálban nem volt találat
        if (locationList == null) {
            locationList = getLocationListByPartNumber(messageOrder.getPartNumber(), messageOrder.getPallets(), true);
            worksheet.updateCooled(true);
        }
        // Elutasítás: nem elég a készlet a rendelés kiszolgálásához
        if (locationList == null) {
            messageOrder.setTransactionMessage("Nincs elég készlet a megadott cikkből: " + messageOrder.getPartNumber());
            worksheet.setRejected();
            return messageOrder;
        }

        // Terminál szabad kapacitás ellenőrzése, előfoglalás
        int reservedTerminal = reserveTerminal(worksheet.isCooled(), messageOrder.getReservationDate());
        if (reservedTerminal != 0) worksheet.setTerminalID(reservedTerminal);
        // Elutasítás: nincs szabad terminál
        else {
            messageOrder.setTransactionMessage("A megadott időpontban nincs szabad terminál");
            worksheet.setRejected();
            return messageOrder;
        }

        // Minden OK, munkalap kitöltése
        worksheet.setRenterID(messageOrder.getRenterID());
        worksheet.setExternalPartNumber(messageOrder.getPartNumber());
        worksheet.setNumberOfPallets(messageOrder.getPallets());
        worksheet.setReservedDate(messageOrder.getReservationDate());
        worksheet.setLocations(locationList);
        worksheet.setApproved();

        // Állapot mentése
        saveWarehouseState();
        WorkOrders.saveWorkOrdersState();

        // Visszaigazolás
        messageOrder.setApproved();
        return messageOrder;
    }

    // Lokációk kigyűjtése cikkszám és palettamennyiség alapján
    private List<Integer> getLocationListByPartNumber(String partNumber, int pallets, boolean isCooled) {
        List<Integer> locationList = new ArrayList<>();
        ConcurrentHashMap<Integer,Location> locations;
        locations = isCooled ? this.cooledLocations : this.normalLocations;

        // Lokációk szűrése
        for (Map.Entry<Integer, Location> entry : locations.entrySet()) {
            Location value = entry.getValue();
            int key = entry.getKey();
            if (value.scanPalletExternalID() == null) break;
            if (value.scanPalletExternalID().equals(partNumber)) locationList.add(key);
            if (locationList.size() == pallets) return locationList;
        }
        // Nincs találat
        return null;
    }

    // Beszállítás: Egy raklap kirakása a terminálra
    synchronized MessageUnload DoUnloading(MessageUnload messageUnload) {
        Worksheet worksheet = WorkOrders.getWorksheet(messageUnload.getTransactionID());

        // Létezik a munkalap?
        if (worksheet == null) {
            messageUnload.setTransactionMessage("Ezen a számon nincs foglalás a rendszerben!");
            return messageUnload;
        }

        // Van még paletta a kocsin?
        Pallet pallet = worksheet.takePallet();
        if (pallet == null) {
            messageUnload.setTransactionMessage("Nincs több paletta a kocsin!");
            return messageUnload;
        }

        // Kipakolás a terminálra
        Terminal terminal;
        if (worksheet.isCooled()){
            terminal = this.cooledTerminals.get(worksheet.getTerminalID());
        } else {
            terminal = this.normalTerminals.get(worksheet.getTerminalID());
        }
        pallet.setInternalPartNumber(messageUnload.getInternalPartNumber());
        terminal.setOccupied();
        terminal.addPallet(pallet);


        // Állapot mentése
        saveWarehouseState();
        WorkOrders.saveWorkOrdersState();

        // Visszaigazolás
        messageUnload.setApproved();
        return messageUnload;
    }

    // Beszállítás: Raklapok berakása a lokációkba, munka készrejelentése
    synchronized MessageComplete DoStoring(MessageComplete messageComplete, Accounting accounting) {
        Worksheet worksheet = WorkOrders.getWorksheet(messageComplete.getTransactionID());

        // Visszautasítás: tranzakció azonosító nem létezik
        if (worksheet == null) {
            messageComplete.setTransactionMessage("Ezen a számon nincs foglalás a rendszerben!");
            return messageComplete;
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
            messageComplete.setTransactionMessage("Palettaszám nem egyezik a foglalt lokációk számával!");
            return messageComplete;
        }

        // Kipakolás a lokációba
        for (int i = 0; i < reservedLocations.size(); i++){
            Pallet pallet = terminal.takePallet(messageComplete.getInternalPartNumber());
            locations.get(reservedLocations.get(i)).addPallet(pallet);
        }

        // Visszaigazolás
        worksheet.setConfirmed();

        // Terminál felszabadítása
        terminal.setFree();

        // Logisztikai művelet lejelentése
        accounting.addLogisticsOperations(messageComplete.getRenterID(),1);

        // Állapot mentése
        saveWarehouseState();
        WorkOrders.saveWorkOrdersState();

        messageComplete.setApproved();
        return messageComplete;
    }

    // Visszamondás
    public synchronized MessageProcess CancelWorkSheet(MessageProcess messageProcess) {
        Worksheet worksheet = WorkOrders.getWorksheet(messageProcess.getTransactionID());
        // Renter renter = accounting.getRenter(reservationMessage.getRenterID());
        // TODO: Warehouse -> terminál felszabadítása
        // TODO: Renter -> lokációk felszabadítása
        // TODO: Accounting -> Logisztikai művelet lejelentése
        worksheet.setCancelled();
        saveWarehouseState();
        WorkOrders.saveWorkOrdersState();
        return messageProcess;
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

    synchronized int getMaxNormalLocation() {
        return this.maxNormalLocation;
    }
    synchronized int getMaxCooledLocation() {
        return this.maxCooledLocation;
    }
    synchronized int getMaxNormalTerminal() {
        return this.maxNormalTerminal;
    }
    synchronized int getMaxCooledTerminal() {
        return this.maxCooledTerminal;
    }

    synchronized int getFreeNormalLocation() {
        return this.freeNormalLocation;
    }
    synchronized int getFreeCooledLocation() {
        return this.freeCooledLocation;
    }
    synchronized int getFreeNormalTerminal() {
        return this.freeNormalTerminal;
    }
    synchronized int getFreeCooledTerminal() {
        return this.freeCooledTerminal;
    }

    public synchronized void increaseFreeCooledLocations(int byAmount){
        this.freeCooledLocation += byAmount;
    }
    public synchronized void increaseFreeNormalLocations(int byAmount){
        this.freeNormalLocation += byAmount;
    }
    synchronized void decreaseFreeCooledLocations(int byAmount){
        this.freeCooledLocation -= byAmount;
    }
    synchronized void decreaseFreeNormalLocations(int byAmount){
        this.freeNormalLocation -= byAmount;
    }

    synchronized ConcurrentHashMap<Integer,Location> getNormalLocations(){
        return this.normalLocations;
    }
    synchronized ConcurrentHashMap<Integer,Location> getCooledLocations(){
        return this.cooledLocations;
    }
    public synchronized ConcurrentHashMap<Integer,Terminal> getNormalTerminals(){
        return this.normalTerminals;
    }
    public synchronized ConcurrentHashMap<Integer,Terminal> getCooledTerminals(){
        return this.cooledTerminals;
    }

    synchronized ConcurrentHashMap<LocalDateTime,List<Integer>> getReservedNormalTerminals(){
        return this.reservedNormalTerminals;
    }
    synchronized ConcurrentHashMap<LocalDateTime,List<Integer>> getReservedCooledTerminals(){
        return this.reservedCooledTerminals;
    }

}
