package com.berraktar;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class Reporting {

    // Jelentések menü
    private final static String menu = "\nBérraktár jelentések menü:\n\t" +
            "1. Ügyfél lista\n\t" +
            "2. Munkalap lista\n\t" +
            "3. Lokáció lista\n\t" +
            "4. Terminál foglalási lista\n\t" +
            "5. Kilépés\n" +
            "Válassz menüpontot: ";

    // Jelentések menü
    static void ShowMenu(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(menu);
        String input = br.readLine();
        while(!input.equals("5")){
            switch(input){
                case "1":
                    GetReport(oos, ois, MessageReport.ReportType.Renters);
                    break;
                case "2":
                    GetReport(oos, ois, MessageReport.ReportType.Worksheets);
                    break;
                case "3":
                    GetReport(oos, ois, MessageReport.ReportType.Locations);
                    break;
                case "4":
                    GetReport(oos, ois, MessageReport.ReportType.Terminals);
                    break;
                default:
                    System.out.println("A megadott menüpont nem létezik! (" + input + ")");
            }
            System.out.print("\nNyomj ENTER-t a folytatáshoz!");
            System.in.read();
            System.out.print(menu);
            input = br.readLine();
        }
    }

    // Szerver kommunikáció
    private static void GetReport(ObjectOutputStream oos, ObjectInputStream ois, MessageReport.ReportType reportType) throws IOException, ClassNotFoundException {
        MessageReport messageReport = new MessageReport(reportType);
        oos.writeObject(messageReport);
        messageReport = (MessageReport) ois.readObject();
        System.out.println(messageReport.getReply());
    }

    // Bérlők jelentés
    static synchronized MessageReport RenterReport(MessageReport messageReport, Map<String, Renter> renters) {
        StringBuilder reply = new StringBuilder();

        // Fejléc
        reply.append("\nID\t\t").append("Név\t\t\t\t").append("Normál lokációk\t\t");
        reply.append("Hűtött lokációk\t\t").append("Logisztikai műveletek\n");

        // Bérlők listája
        for (Map.Entry<String, Renter> entry : renters.entrySet()){
            Renter value = entry.getValue();
            reply.append(value.getCode()).append("\t").append(value.getName()).append("\t\t");
            reply.append(value.getRentedNormalLocations()).append("\t\t\t\t\t");
            reply.append(value.getRentedCooledLocations()).append("\t\t\t\t\t").append(value.getNumberOfLogisticsOperations());
            reply.append("\n");
        }

        // Jelentés mentése az üzenetbe
        messageReport.setReply(reply.toString());
        return messageReport;
    }

    // Munkalapok jelentés
    static synchronized MessageReport WorksheetReport(MessageReport messageReport, ConcurrentHashMap<Integer, Worksheet> worksheets) {
        StringBuilder reply = new StringBuilder();
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm");

        // Fejléc
        reply.append("\nID#\t\tStátusz\t\t\tBérlő#\tDátum\t\tIdő\t\tPaletta#\tMunka\tTerminál\tLokációk");

        // Munkalapok adatainak kigyűjtése
        for (Map.Entry<Integer, Worksheet> entry : worksheets.entrySet()) {
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

        // Jelentés mentése az üzenetbe
        messageReport.setReply(reply.toString());
        return messageReport;
    }

    // Lokáció jelentés
    static synchronized MessageReport LocationReport(MessageReport messageReport, Warehouse warehouse) {
        StringBuilder reply = new StringBuilder();
        Map<Integer,Location> normalLocations = warehouse.getNormalLocations();
        Map<Integer,Location> cooledLocations = warehouse.getCooledLocations();

        // Fejléc
        reply.append("\nLokáció összefoglaló\n\n");

        // Lokáció fő adatok
        reply.append("Szabad / maximális normál lokációk száma:   \t");
        reply.append(warehouse.getFreeNormalLocation()).append(" / ").append(warehouse.getMaxNormalLocation()).append("\n");
        reply.append("Szabad / maximális hűtött lokációk száma:   \t");
        reply.append(warehouse.getFreeCooledLocation()).append(" / ").append(warehouse.getMaxCooledLocation()).append("\n");

        // Lokáció részletek
        reply.append("\nFoglalt normál lokációk listája\n");
        reply.append("\nLocID#\tRenterID\t\tInternalID\t\t\t\tExternalID");
        fillLocationList(reply, normalLocations);

        reply.append("\n\nFoglalt hűtött lokációk listája\n");
        reply.append("\nLocID#\tRenterID\t\tInternalID\t\t\t\tExternalID");
        fillLocationList(reply, cooledLocations);

        // Válasz mentése a jelentésbe
        messageReport.setReply(reply.toString());
        return messageReport;
    }

    // Lokáció lista lokáció jelentéshez
    private static synchronized void fillLocationList(StringBuilder reply, Map<Integer, Location> locations) {
        for (Map.Entry<Integer, Location> entry : locations.entrySet()) {
            Location value = entry.getValue();
            if (value.getRenterID() != null) {
                reply.append("\n").append(entry.getKey()).append("\t\t");
                reply.append(value.getRenterID()).append("\t\t\t");
                reply.append(value.scanPalletInternalID()).append("\t\t\t\t");
                reply.append(value.scanPalletExternalID()).append("\t\t\t");
            }
        }
    }

    // Terminál foglalások jelentés
    static synchronized MessageReport TerminalReport(MessageReport messageReport, Warehouse warehouse) {
        StringBuilder reply = new StringBuilder();

        // Fejléc
        reply.append("\nTerminál foglalások\n\n");

        // Terminál fő adatok
        reply.append("Szabad / maximális normál terminálok száma: \t");
        reply.append(warehouse.getFreeNormalTerminal()).append(" / ").append(warehouse.getMaxNormalTerminal()).append("\n");
        reply.append("Szabad / maximális hűtött terminálok száma: \t");
        reply.append(warehouse.getFreeCooledTerminal()).append(" / ").append(warehouse.getMaxCooledTerminal()).append("\n");

        // Terminál részletek
        reply.append("\nNormál terminál foglalások listája\n");
        reply.append("\nDátum\t\t\t\tTerminálok");
        getTerminalList(reply, warehouse.getReservedNormalTerminals());

        reply.append("\n\nHűtött terminál foglalások listája\n");
        reply.append("\nDátum\t\t\t\tTerminálok");
        getTerminalList(reply, warehouse.getReservedCooledTerminals());

        // Válasz mentése a jelentésbe
        messageReport.setReply(reply.toString());
        return messageReport;
    }

    // Terminál lista terminál jelentéshez
    private static void getTerminalList(StringBuilder reply, ConcurrentHashMap<LocalDateTime, List<Integer>> terminals) {
        for (Map.Entry<LocalDateTime, List<Integer>> entry : terminals.entrySet()) {
            if (entry != null) {
                reply.append("\n").append(entry.getKey()).append("\t\t").append(entry.getValue());
            }
        }
    }

}
