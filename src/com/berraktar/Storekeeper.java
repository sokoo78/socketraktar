package com.berraktar;

import java.io.*;

public class Storekeeper extends Employee {

    public Storekeeper(String name, UserType position) {
        super(name, position);
    }

    // TODO: Bevételzés
    public static void doReceiving(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Munkalap adatok lekérése a szerverről
        System.out.print("Tranzakció azonosító: "); // Munkalap sorszáma
        Receiving receiving = new Receiving(Integer.parseInt(br.readLine()));
        receiving.setApproved();
        oos.writeObject(receiving);
        receiving = (Receiving)ois.readObject();
        if (receiving.isProcessing()){
            System.out.print("Beszállítás rendben - munkalap folyamatban!");
        } else {
            System.out.print("Beszállítási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + receiving.getTransactionMessage());
        }

        // Paletták szkennelése és kipakolása
        for (int i = 0; i < receiving.getPallets(); i++) {
            System.out.print("Szkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                    receiving.getExternalPartNumber() + "): ");
            String scannedPartNumber = br.readLine();
            while (scannedPartNumber != receiving.getExternalPartNumber()) {
                System.out.println("A beszkennelt cikkszám nem egyezik a foglaláson szereplő cikkszámmal!");
                System.out.print("Szkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                        receiving.getExternalPartNumber() + "): ");
                scannedPartNumber = br.readLine();
            }
            receiving.setInternalPartNumber(generateInternalPartNumber(receiving));

            // Paletta lejelentése a szervernek
            Unloading unloading = new Unloading(receiving.getTransactionID(), receiving.getInternalPartNumber());
            oos.writeObject(unloading);
            unloading = (Unloading)ois.readObject();
            if (unloading.isConfirmed()){
                System.out.print("Paletta sikeresen kirakva a " + receiving.getTerminalID() + " terminálra!");
            } else {
                System.out.print("Paletta kirakás elutasítva!");
                System.out.print("\nSzerver üzenete: " + receiving.getTransactionMessage());
            }
        }

        // Munkalap lejelentése a szervernek
        System.out.print("Tranzakció készrejelentése (i/n): "); // Munkalap sorszáma
        boolean isConfirmed = UserIO.readBoolean();
        if (isConfirmed){
            receiving.setUnloaded();
            oos.writeObject(receiving);
            receiving = (Receiving)ois.readObject();
            if (receiving.isCompleted()){
                System.out.print("Beszállítás rendben - munkalap lezárva!");
            } else {
                System.out.print("Beszállítás lejelentése elutasítva!");
                System.out.print("\nSzerver üzenete: " + receiving.getTransactionMessage());
            }
        }
    }

    // TODO: Kiszállítás
    public static void doShipping(ObjectOutputStream oos, ObjectInputStream ois) {

    }

    // Belső cikkszám generálás - vevőkódot hozzáadja prefixumként, lehet szofisztikálni ha kell..
    static String generateInternalPartNumber(Receiving receiving) {
        return receiving.getRenterID() + "-" + receiving.getExternalPartNumber();
    }
}
