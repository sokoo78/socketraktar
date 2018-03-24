package com.berraktar;

import java.io.*;

public class Storekeeper extends Employee {
    // Szerializációhoz kell
    private static final long serialVersionUID = 3980978824897032892L;

    // Konstruktor
    public Storekeeper(String name, UserType position) {
        super(name, position);
    }

    // Bevételzés
    public static void doReceiving(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Munkalap adatok lekérése a szerverről
        System.out.print("Tranzakció azonosító: "); // Munkalap sorszáma
        ReceivingMessage receivingMessage = new ReceivingMessage(Integer.parseInt(br.readLine()));
        receivingMessage.setApproved();
        oos.writeObject(receivingMessage);
        receivingMessage = (ReceivingMessage)ois.readObject();
        if (receivingMessage.isProcessing()){
            System.out.print("Beszállítás rendben - munkalap folyamatban!");
        } else {
            System.out.print("Beszállítási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + receivingMessage.getTransactionMessage());
        }

        // Paletták szkennelése és kipakolása
        for (int i = 0; i < receivingMessage.getPallets(); i++) {
            System.out.print("Szkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                    receivingMessage.getExternalPartNumber() + "): ");
            String scannedPartNumber = br.readLine();
            while (scannedPartNumber != receivingMessage.getExternalPartNumber()) {
                System.out.println("A beszkennelt cikkszám nem egyezik a foglaláson szereplő cikkszámmal!");
                System.out.print("Szkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                        receivingMessage.getExternalPartNumber() + "): ");
                scannedPartNumber = br.readLine();
            }
            receivingMessage.setInternalPartNumber(generateInternalPartNumber(receivingMessage));

            // Paletta lejelentése a szervernek
            UnloadingMessage unloadingMessage = new UnloadingMessage(receivingMessage.getTransactionID(), receivingMessage.getInternalPartNumber());
            oos.writeObject(unloadingMessage);
            unloadingMessage = (UnloadingMessage)ois.readObject();
            if (unloadingMessage.isConfirmed()){
                System.out.print("Paletta sikeresen kirakva a " + receivingMessage.getTerminalID() + " terminálra!");
            } else {
                System.out.print("Paletta kirakás elutasítva!");
                System.out.print("\nSzerver üzenete: " + receivingMessage.getTransactionMessage());
            }
        }

        // Munkalap lejelentése a szervernek
        System.out.print("Tranzakció készrejelentése (i/n): "); // Munkalap sorszáma
        boolean isConfirmed = UserIO.readBoolean();
        if (isConfirmed){
            receivingMessage.setUnloaded();
            oos.writeObject(receivingMessage);
            receivingMessage = (ReceivingMessage)ois.readObject();
            if (receivingMessage.isCompleted()){
                System.out.print("Beszállítás rendben - munkalap lezárva!");
            } else {
                System.out.print("Beszállítás lejelentése elutasítva!");
                System.out.print("\nSzerver üzenete: " + receivingMessage.getTransactionMessage());
            }
        }
    }

    // TODO: Kiszállítás
    public static void doShipping(ObjectOutputStream oos, ObjectInputStream ois) {

    }

    // Belső cikkszám generálás - vevőkódot hozzáadja prefixumként, lehet szofisztikálni ha kell..
    static String generateInternalPartNumber(ReceivingMessage receivingMessage) {
        return receivingMessage.getRenterID() + "-" + receivingMessage.getExternalPartNumber();
    }
}
