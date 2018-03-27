package com.berraktar;

import java.io.*;

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
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Munkalap adatok lekérése a szerverről
        System.out.print("Tranzakció azonosító: "); // Munkalap sorszáma
        MessageProcess messageProcess = new MessageProcess(Integer.parseInt(br.readLine()));
        oos.writeObject(messageProcess);
        messageProcess = (MessageProcess)ois.readObject();
        if (messageProcess.isApproved()){
            System.out.print("Beszállítás rendben - munkalap folyamatban!");
        } else {
            System.out.print("Beszállítási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + messageProcess.getTransactionMessage());
            return;
        }

        // Paletták szkennelése és kipakolása
        for (int i = 0; i < messageProcess.getPallets(); i++) {
            System.out.print("\nSzkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                    messageProcess.getExternalPartNumber() + "): ");
            String scannedPartNumber = br.readLine();
            System.out.println(scannedPartNumber + " vs " + messageProcess.getExternalPartNumber());
            while (!scannedPartNumber.equalsIgnoreCase(messageProcess.getExternalPartNumber())) {
                System.out.println("A beszkennelt cikkszám nem egyezik a foglaláson szereplő cikkszámmal!");
                System.out.print("Szkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                        messageProcess.getExternalPartNumber() + "): ");
                scannedPartNumber = br.readLine();
            }
            messageProcess.setInternalPartNumber(generateInternalPartNumber(messageProcess));

            // Paletta lejelentése a szervernek
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

        // Munkalap lejelentése a szervernek
        System.out.print("Tranzakció készrejelentése (i/n): ");
        boolean isConfirmed = UserIO.readBoolean();
        if (isConfirmed){
            MessageComplete messageComplete = new MessageComplete(messageProcess.getTransactionID());
            messageComplete.setRenterID(messageProcess.getRenterID());
            messageComplete.setInternalPartNumber(messageProcess.getInternalPartNumber());
            oos.writeObject(messageComplete);
            messageComplete = (MessageComplete)ois.readObject();
            if (messageComplete.isApproved()){
                System.out.print("Beszállítás rendben - munkalap lezárva!");
            } else {
                System.out.print("Beszállítás lejelentése elutasítva!");
                System.out.print("\nSzerver üzenete: " + messageComplete.getTransactionMessage());
            }
        }
    }

    // TODO: Kiszállítás
    private static void doShipping(ObjectOutputStream oos, ObjectInputStream ois) {

    }

    // Belső cikkszám generálás - vevőkódot hozzáadja prefixumként, lehet szofisztikálni ha kell..
    static String generateInternalPartNumber(MessageProcess messageProcess) {
        return messageProcess.getRenterID() + "-" + messageProcess.getExternalPartNumber();
    }

}
