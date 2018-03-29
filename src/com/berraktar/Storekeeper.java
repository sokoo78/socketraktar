package com.berraktar;

import java.io.*;
import java.time.LocalDateTime;

public class Storekeeper extends Employee {

    // Szerializációhoz kell
    private static final long serialVersionUID = 3980978824897032892L;

    // Menü
    private static final String menu = "\nBérraktár raktáros menü:\n\t" +
            "1. Beérkezések\n\t" +
            "2. Kiszállítások\n\t" +
            "3. Kilépés\n" +
            "Válassz menüpontot: ";

    // Konstruktor
    public Storekeeper(String name, UserType position) {
        super(name, position);
    }

    // Raktáros menü
    static synchronized void ShowMenu(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(menu);
        String input = br.readLine();
        while(!input.equals("3")) {
            switch (input) {
                case "1":
                    doReceiving(oos, ois);
                    break;
                case "2":
                    doShipping(oos, ois);
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

    // Bevételzés
    private static synchronized void doReceiving(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {

        // Munkalap adatok lekérése a szerverről
        MessageProcess messageProcess = getMessageProcess(oos, ois);

        // Paletták szkennelése és ellenőrzése
        for (int i = 0; i < (messageProcess != null ? messageProcess.getPallets() : 0); i++) {
            ScanAndCheckNextPallet(oos, ois, messageProcess, i+1);
            messageProcess.setInternalPartNumber(generateInternalPartNumber(messageProcess, i+1));

            // Paletta lejelentése a szervernek - kirakás a terminálra
            MessageUnload messageUnload = new MessageUnload(messageProcess.getTransactionID(), messageProcess.getInternalPartNumber());
            oos.writeObject(messageUnload);
            messageUnload = (MessageUnload)ois.readObject();
            if (messageUnload.isApproved()){
                System.out.print("Paletta sikeresen kirakva a(z) " + messageProcess.getTerminalID() + " számú terminálra!");
            } else {
                System.out.print("Paletta kirakás elutasítva!");
                System.out.print("\nSzerver üzenete: " + messageUnload.getTransactionMessage());
                return;
            }
        }

        // Munkalap lejelentése a szervernek - betárolás a lokációkba
        System.out.print("\nBeszállítási tranzakció készrejelentése (i/n): ");
        if (UserIO.readBoolean()){
            MessageStore messageStore = new MessageStore(messageProcess.getTransactionID());
            messageStore.setRenterID(messageProcess.getRenterID());
            messageStore.setInternalPartNumber(messageProcess.getInternalPartNumber());
            oos.writeObject(messageStore);
            messageStore = (MessageStore)ois.readObject();
            if (messageStore.isApproved()){
                System.out.print("Beszállítás rendben - munkalap lezárva!");
            } else {
                System.out.print("Beszállítás lejelentése elutasítva!");
                System.out.print("\nSzerver üzenete: " + messageStore.getTransactionMessage());
            }
        }
    }

    // Kiszállítás
    private static synchronized void doShipping(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {

        // Munkalap adatok lekérése a szerverről
        MessageProcess messageProcess = getMessageProcess(oos, ois);

        // Paletták szkennelése és ellenőrzése
        for (int i = 0; i < (messageProcess != null ? messageProcess.getPallets() : 0); i++) {

            // Cikkszám és paletta ellenőrzése
            ScanAndCheckNextPallet(oos, ois, messageProcess, i+1);

            // Paletta lejelentése a szervernek - kirakás a terminálra
            MessageLoad messageLoad = new MessageLoad(messageProcess.getTransactionID());
            oos.writeObject(messageLoad);
            messageLoad = (MessageLoad)ois.readObject();
            if (messageLoad.isApproved()){
                System.out.print("\nPaletta sikeresen kitárolva a(z) " + messageProcess.getTerminalID() + " számú terminálra!");
            } else {
                System.out.print("\nPaletta kitárolás elutasítva!");
                System.out.print("\nSzerver üzenete: " + messageLoad.getTransactionMessage());
                return;
            }
        }

        // Munkalap lejelentése a szervernek - kiszállítás a terminálról
        System.out.print("\nKiszállítási tranzakció készrejelentése (i/n): ");
        if (UserIO.readBoolean()){
            MessageShip messageShip = new MessageShip(messageProcess != null ? messageProcess.getTransactionID() : 0);
            oos.writeObject(messageShip);
            messageShip = (MessageShip)ois.readObject();
            if (messageShip.isApproved()){
                System.out.print("\nKiszállítás rendben - munkalap lezárva!");
            } else {
                System.out.print("\nKiszállítás lejelentése elutasítva!");
                System.out.print("\nSzerver üzenete: " + messageShip.getTransactionMessage());
            }
        }
    }

    // Munkalap adatok lekérése
    private synchronized static MessageProcess getMessageProcess(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Tranzakció azonosító: "); // Munkalap sorszáma
        MessageProcess messageProcess = new MessageProcess(Integer.parseInt(br.readLine()));
        oos.writeObject(messageProcess);
        messageProcess = (MessageProcess)ois.readObject();
        if (messageProcess.isApproved()){
            System.out.print("Szállítmány rendben - munkalap folyamatban!");
        } else {
            System.out.print("Szállítási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + messageProcess.getTransactionMessage());
            return null;
        }
        return messageProcess;
    }

    // Paletta szkennelése, cikkszám és paletta ellenőrzése
    private static synchronized void ScanAndCheckNextPallet(ObjectOutputStream oos, ObjectInputStream ois, MessageProcess messageProcess, int i) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("\nSzkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                messageProcess.getExternalPartNumber() + "): ");
        String scannedPartNumber = br.readLine();
        while (!scannedPartNumber.equalsIgnoreCase(messageProcess.getExternalPartNumber())) {
            System.out.println("A beszkennelt cikkszám nem egyezik a foglaláson szereplő cikkszámmal!");
            System.out.print("Szkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                    messageProcess.getExternalPartNumber() + "): ");
            scannedPartNumber = br.readLine();
        }
        System.out.print("Jegyzőkönyv készítése (i/n): ");
        if (UserIO.readBoolean()){
            writeDeviationProtocol(oos, ois, messageProcess.getTransactionID(), LocalDateTime.now());
        }
    }

    // Jegyzőkönyv írás
    private static synchronized void writeDeviationProtocol(ObjectOutputStream oos, ObjectInputStream ois, int transactionID, LocalDateTime transactionDate) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Protocol protocol = new Protocol(transactionID, transactionDate);
        System.out.println("Írja be a jegyzőkönyv szövegét: ");
        protocol.setTransactionMessage(br.readLine());
        oos.writeObject(protocol);
        protocol = (Protocol)ois.readObject();
        if (protocol.isApproved()) {
            System.out.print("\nJegyzőkönyv mentve!\n");
        } else {
            System.out.print("\nJegyzőkönyv elutasítva!");
            System.out.print("\nSzerver üzenete: " + protocol.getTransactionMessage() + "\n");
        }
    }

    // Belső cikkszám generálás - vevőkódot hozzáadja prefixumként, lehet szofisztikálni ha kell..
    static synchronized String generateInternalPartNumber(MessageProcess messageProcess, Integer i) {
        return messageProcess.getRenterID() + "-" + messageProcess.getExternalPartNumber() + "-" + i.toString();
    }

}