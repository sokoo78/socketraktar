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
            System.out.print("Szkenneld be az " + i + ". palettát (Bérlő cikkszáma: " +
                    messageProcess.getExternalPartNumber() + "): ");
            String scannedPartNumber = br.readLine();
            while (scannedPartNumber.equals(messageProcess.getExternalPartNumber())) {
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
    public static void doShipping(ObjectOutputStream oos, ObjectInputStream ois) {

    }

    // Belső cikkszám generálás - vevőkódot hozzáadja prefixumként, lehet szofisztikálni ha kell..
    static String generateInternalPartNumber(MessageProcess messageProcess) {
        return messageProcess.getRenterID() + "-" + messageProcess.getExternalPartNumber();
    }
}
