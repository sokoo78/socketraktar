package com.berraktar;

import java.io.*;
import java.time.LocalDateTime;

public class Dispatcher extends Employee implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = -8829548734538973664L;

    // Konstruktor
    public Dispatcher(String name, UserType position) {
        super(name, position);
    }

    // Új foglalás
    static void newReservation(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Új foglalási kérelem
        ReservationMessage reservationMessage = new ReservationMessage();
        reservationMessage.setWorkSheetType(Worksheet.WorkSheetType.Incoming);

        // Tranzakciószám kérése a szervertől
        oos.writeObject(reservationMessage);
        reservationMessage = (ReservationMessage) ois.readObject();
        System.out.print("\nÚj munkalap létrehozva - Tranzakcióazonosító: " +
                reservationMessage.getTransactionID() + " (isInitialized: " + reservationMessage.isCreated() + ")");

        // Kérelem adatainak bekérése a munkalapra
        System.out.print("\nVevőkód: ");
        reservationMessage.setRenterID(br.readLine());
        System.out.print("Cikkszám: ");
        reservationMessage.setPartNumber(br.readLine());
        System.out.print("Hűtött áru? (i/n): ");
        reservationMessage.setCooled(UserIO.readBoolean());
        System.out.print("Raklapok száma: ");
        reservationMessage.setPallets(Integer.parseInt(br.readLine()));
        System.out.println("Foglalás időpontja (Példa: " + UserIO.printDate(LocalDateTime.now()) + "):");
        reservationMessage.setReservationDate(UserIO.readDate(true));
        System.out.print("Idő kerekítve: " + UserIO.printDate(reservationMessage.getReservationDate()));

        // Foglalás adatainak ellenőrzése, előfoglalás
        System.out.print("\nFoglalási adatok ellenőrzése.. ");
        oos.writeObject(reservationMessage);
        reservationMessage = (ReservationMessage) ois.readObject();
        if (reservationMessage.isApproved()) {
            System.out.print("Foglalási adatok elfogadva!");
        }
        else {
            System.out.print("Foglalási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + reservationMessage.getTransactionMessage());
        }

        // TODO: Foglalás jóváhagyása, vagy törlése
    }

    // Beszállítás (megérkezett a szállítmány)
    static void doReceiving(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // Beérkezés adatainak bekérése
        System.out.print("Tranzakció azonosító: ");         // Munkalap sorszáma
        int transactionID = Integer.parseInt(br.readLine());
        System.out.print("Beérkezés időpontja: ");          // TODO: a dátumot csak a teszt célból kell megadni, realtime működés esetén LocalDateTime.now() kell ide
        LocalDateTime receivingDate = UserIO.readDate(false);
        ReceivingMessage receivingMessage = new ReceivingMessage(transactionID, receivingDate);
        oos.writeObject(receivingMessage);
        receivingMessage = (ReceivingMessage) ois.readObject();
        if (receivingMessage.isApproved()){
            System.out.print("Beszállítási adatok elfogadva - munkalap aktiválva!");
        } else {
            System.out.print("Beszállítási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + receivingMessage.getTransactionMessage());
        }
    }

    // TODO: Kifejtendő - Új Kiszállítás
    static void newDelivery(ObjectOutputStream oos, ObjectInputStream ois) {
        // TODO 1: adatokat felvenni
        // TODO 2: szerverrel ellenőriztetni és jóváhagyatni (ehhez a szerver oldalnak is meg kell lennie)
    }

    // TODO: Kifejtendő - Kiszállítás (kocsi beérkezett)
    static void doDelivery(ObjectOutputStream oos, ObjectInputStream ois) {
        // TODO 1: dátumot csekkolni - ha nem egyezik a foglalással akkor kuka
        // TODO 2: munkalapot aktiválni kell, hogy lássa a raktáros (raktáros kiszállítás funkció is kell)
    }

}
