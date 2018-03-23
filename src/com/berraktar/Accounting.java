package com.berraktar;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

class Accounting implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = -5771420064864866043L;

    // Bérlők
    private Map<String, Renter> renters;

    // Konstruktor
    Accounting(){
        loadAccountingState();
    }

    // Bérlők adatainak betöltése
    private synchronized void loadAccountingState(){
        if (new File("Renters.ser").exists()) {
            this.renters = (Map<String, Renter>) Persistency.LoadObject("Renters.ser");
        }
        // Tesztbérlők létrehozása ha nem létezik a fájl - TODO: ezt lehet törölni ha kész a cucc
        else {
            this.createTestRenters();
        }
    }

    // Bérlők adatainak mentése
    private synchronized void saveAccountingState(){
        Persistency.SaveObject(this.getRenters(), "Renters.ser");
    }

    private synchronized static void GetReport(ObjectOutputStream oos, ObjectInputStream ois, ReportMessage.ReportType reportType) throws IOException, ClassNotFoundException {
        ReportMessage reportMessage = new ReportMessage(reportType);
        oos.writeObject(reportMessage);
        reportMessage = (ReportMessage) ois.readObject();
        System.out.println(reportMessage.getReply());
    }

    public synchronized int getTotalCooledReservations(){
        return renters.values().stream().mapToInt(i -> i.getRentedCooledLocations()).sum();
    }

    public synchronized int getTotalNormalReservations(){
        return renters.values().stream().mapToInt(i -> i.getRentedNormalLocations()).sum();
    }

    public synchronized void addLogisticsOperations (String renterID, int numberOfOperations){
        this.getRenter(renterID).addLogisticsOperations(numberOfOperations);
        saveAccountingState();
    }

    public synchronized Renter getRenter(String renterID){
        return this.renters.get(renterID);
    }

    public synchronized Map<String, Renter> getRenters() {
        return this.renters;
    }

    private synchronized void setRenters(Map<String, Renter> renters) {
        this.renters = renters;
    }

    // Teszt bérlők
    private synchronized void createTestRenters(){
        Renter renter_1 = new Renter(
                "Bérlő Béla",
                "BEBE",
                500,
                0,
                500,
                0,
                0);

        Renter renter_2 = new Renter(
                "Renter Zoli",
                "REZO",
                200,
                100,
                200,
                100,
                0);

        Renter renter_3 = new Renter(
                "Gazdag Peti",
                "GAPE",
                0,
                500,
                0,
                500,
                0);

        Map<String, Renter> testRenters = new HashMap<>();
        testRenters.put(renter_1.getCode(), renter_1);
        testRenters.put(renter_2.getCode(), renter_2);
        testRenters.put(renter_3.getCode(), renter_3);
        this.setRenters(testRenters);
    }
}
