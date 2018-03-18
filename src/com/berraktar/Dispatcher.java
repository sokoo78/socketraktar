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
        Reservation reservation = new Reservation();
        reservation.setWorkSheetType(Worksheet.WorkSheetType.Incoming);

        // Tranzakciószám kérése a szervertől
        oos.writeObject(reservation);
        reservation = (Reservation) ois.readObject();
        System.out.print("\nÚj munkalap létrehozva - Tranzakcióazonosító: " +
                reservation.getTransactionID() + " (isInitialized: " + reservation.isCreated() + ")");

        // Kérelem adatainak bekérése a munkalapra
        System.out.print("\nVevőkód: ");
        reservation.setRenterID(br.readLine());
        System.out.print("Cikkszám: ");
        reservation.setPartNumber(br.readLine());
        System.out.print("Hűtött áru? (i/n): ");
        reservation.setCooled(UserIO.readBoolean());
        System.out.print("Raklapok száma: ");
        reservation.setPallets(Integer.parseInt(br.readLine()));
        System.out.println("Foglalás időpontja (Példa: " + UserIO.printDate(LocalDateTime.now()) + "):");
        reservation.setReservationDate(UserIO.readDate(true));
        System.out.print("Idő kerekítve: " + UserIO.printDate(reservation.getReservationDate()));

        // Foglalás adatainak ellenőrzése, előfoglalás
        System.out.print("\nFoglalási adatok ellenőrzése.. ");
        oos.writeObject(reservation);
        reservation = (Reservation) ois.readObject();
        if (reservation.isApproved()) {
            System.out.print("Foglalási adatok elfogadva!");
        }
        else {
            System.out.print("Foglalási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + reservation.getTransactionMessage());
        }

        // TODO: Foglalás jóváhagyása, vagy törlése
    }

    // Beszállítás (megérkezett a szállítmány)
    static void doReceiving(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // Beérkezés adatainak bekérése
        System.out.print("Tranzakció azonosító: ");         // Munkalap sorszáma
        int transactionID = Integer.parseInt(br.readLine());
        System.out.print("Beérkezés időpontja: ");          // TODO: a dátumot teszt célból kell megadni, realtime működés esetén nem kell
        LocalDateTime receivingDate = UserIO.readDate(false);
        Receiving receiving = new Receiving(transactionID, receivingDate);
        oos.writeObject(receiving);
        receiving = (Receiving) ois.readObject();
        if (receiving.isApproved()){
            System.out.print("Beszállítási adatok elfogadva - munkalap aktiválva!"); // TODO: munkalapot aktiválni
        } else {
            System.out.print("Beszállítási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + receiving.getTransactionMessage());
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
