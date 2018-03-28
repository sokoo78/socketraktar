package com.berraktar;

import java.io.*;
import java.util.Objects;

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
    static void ShowMenu(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(menu);
        String input = br.readLine();
        while(!input.equals("3")) {
            switch (input) {
                case "1":
                    Storekeeper.doReceiving(oos, ois);
                    break;
                case "2":
                    Storekeeper.doShipping(oos, ois);
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
    private static void doReceiving(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {

        // Munkalap adatok lekérése a szerverről
        MessageProcess messageProcess = getMessageProcess(oos, ois);

        // Paletták szkennelése - TODO és ellenőrzése (jegyzőkönyv)
        for (int i = 0; i < Objects.requireNonNull(messageProcess).getPallets(); i++) {
            ScanNextPallet(messageProcess.getExternalPartNumber(), i);
            messageProcess.setInternalPartNumber(generateInternalPartNumber(messageProcess));

            // Paletta lejelentése a szervernek - kirakás a terminálra
            MessageUnload messageUnload = new MessageUnload(messageProcess.getTransactionID(), messageProcess.getInternalPartNumber());
            oos.writeObject(messageUnload);
            messageUnload = (MessageUnload)ois.readObject();
            if (messageUnload.isApproved()){
                System.out.print("Paletta sikeresen kirakva a " + messageProcess.getTerminalID() + " terminálra!");
            } else {
                System.out.print("Paletta kirakás elutasítva!");
                System.out.print("\nSzerver üzenete: " + messageUnload.getTransactionMessage());
                return;
            }
        }

        // Munkalap lejelentése a szervernek - betárolás a lokációkba
        System.out.print("Beszállítási tranzakció készrejelentése (i/n): ");
        boolean isConfirmed = UserIO.readBoolean();
        if (isConfirmed){
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
    private static void doShipping(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {

        // Munkalap adatok lekérése a szerverről
        MessageProcess messageProcess = getMessageProcess(oos, ois);

        // Paletták szkennelése - TODO és ellenőrzése (jegyzőkönyv)
        for (int i = 0; i < (messageProcess != null ? messageProcess.getPallets() : 0); i++) {

            // Cikkszám és paletta ellenőrzése
            ScanNextPallet(messageProcess.getExternalPartNumber(), i+1);

            // Paletta lejelentése a szervernek - kirakás a terminálra
            MessageLoad messageLoad = new MessageLoad(messageProcess.getTransactionID());
            oos.writeObject(messageLoad);
            messageLoad = (MessageLoad)ois.readObject();
            if (messageLoad.isApproved()){
                System.out.print("Paletta sikeresen kitárolva a(z) " + messageProcess.getTerminalID() + " számú terminálra!");
            } else {
                System.out.print("Paletta kitárolás elutasítva!");
                System.out.print("\nSzerver üzenete: " + messageLoad.getTransactionMessage());
                return;
            }
        }

        // Munkalap lejelentése a szervernek - kiszállítás a terminálról
        System.out.print("\n\nKiszállítási tranzakció készrejelentése (i/n): ");
        boolean isConfirmed = UserIO.readBoolean();
        if (isConfirmed){
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
    private static MessageProcess getMessageProcess(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
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

    // Paletta szkennelése, cikkszám és paletta ellenőrzése - TODO paletta ellenőrzés (jegyzőkönyv)
    private static void ScanNextPallet(String externalPartNumber, int i) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("\nSzkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                externalPartNumber + "): ");
        String scannedPartNumber = br.readLine();
        while (!scannedPartNumber.equalsIgnoreCase(externalPartNumber)) {
            System.out.println("A beszkennelt cikkszám nem egyezik a foglaláson szereplő cikkszámmal!");
            System.out.print("Szkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                    externalPartNumber + "): ");
            scannedPartNumber = br.readLine();
        }
    }

    // Belső cikkszám generálás - vevőkódot hozzáadja prefixumként, lehet szofisztikálni ha kell..
    static String generateInternalPartNumber(MessageProcess messageProcess) {
        return messageProcess.getRenterID() + "-" + messageProcess.getExternalPartNumber();
    }

}