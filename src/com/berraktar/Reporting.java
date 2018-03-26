package com.berraktar;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Reporting {

    // Bérlők jelentés
    public static synchronized MessageReport RenterReport(MessageReport messageReport, Map<String, Renter> renters) {
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
    public static synchronized MessageReport WorksheetReport(MessageReport messageReport, ConcurrentHashMap<Integer, Worksheet> worksheets) {
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
    public static synchronized MessageReport LocationReport(MessageReport messageReport, Warehouse warehouse) {
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
    public static synchronized MessageReport TerminalReport(MessageReport messageReport, Warehouse warehouse) {
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
