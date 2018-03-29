// Jegyzőkönyvek
package com.berraktar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class DeviationProtocolls {

    // Jegyzőkönyvek
    private static Map<Integer, List<Protocol>> protocols = new ConcurrentHashMap<>();

    // Kostruktor
    static {
        loadDeviationProtocollState();
    }

    // Mentett jegyzőkönyvek betöltése
    private static synchronized void loadDeviationProtocollState() {
        // Mentett munkalapok betöltése
        if (new File("Protocolls.ser").exists()) {
            protocols = (ConcurrentHashMap<Integer, List<Protocol>>) Persistency.LoadObject("Protocolls.ser");
        }
    }

    // Jegyzőkönyvek mentése
    private static synchronized void saveDeviationProtocollState() {
        Persistency.SaveObject(protocols, "Protocolls.ser");
    }

    // Új jegyzőkönyv hozzáadása
    static synchronized Protocol addProtocol(Protocol protocol) {

        // Elutasítás: nem létező tranzakció
        if (WorkOrders.getWorksheet(protocol.getTransactionID()) == null) {
            protocol.setTransactionMessage("Érvénytelen tranzakció azonosító");
            return protocol;
        }

        // Ha az adott munkalaphoz még nem tartozik jegyzőkönyv, létre kell hozni
        if (protocols.get(protocol.getTransactionID()) == null) {
            List<Protocol> protocolList = new ArrayList<>();
            protocols.put(protocol.getTransactionID(), protocolList);
        }
        protocols.get(protocol.getTransactionID()).add(protocol);

        // Állapot mentése
        saveDeviationProtocollState();

        protocol.setApproved();
        return protocol;
    }

    // Jegyzőkönyvek lekérése ID alapján
    static synchronized List<Protocol> getProtocolsByID(int transactionID) {
        // Elutasítás: nem létező tranzakció
        if (WorkOrders.getWorksheet(transactionID) == null) {
            return null;
        }
        return protocols.get(transactionID);
    }

    // Az összes jegyzőkönyv lekérése
    static synchronized Map<Integer, List<Protocol>> getProtocols() {
        return protocols;
    }
}
