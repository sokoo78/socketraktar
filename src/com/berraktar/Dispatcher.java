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

        // Új munkalap (tranzakció azonosító) kérelem
        MessageCreate messageCreate = new MessageCreate();
        messageCreate.setIncoming();
        oos.writeObject(messageCreate);
        messageCreate = (MessageCreate) ois.readObject();

        // Új foglalási kérelem
        MessageReserve messageReserve = new MessageReserve();
        messageReserve.setTransactionID(messageCreate.getTransactionID());
        System.out.print("\nÚj munkalap létrehozva - Tranzakcióazonosító: " + messageCreate.getTransactionID());

        // Kérelem adatainak bekérése a munkalapra
        System.out.print("\nVevőkód: ");
        messageReserve.setRenterID(br.readLine());
        System.out.print("Cikkszám: ");
        messageReserve.setPartNumber(br.readLine());
        System.out.print("Hűtött áru? (i/n): ");
        messageReserve.setCooled(UserIO.readBoolean());
        System.out.print("Raklapok száma: ");
        messageReserve.setPallets(Integer.parseInt(br.readLine()));
        System.out.println("Foglalás időpontja (Példa: " + UserIO.printDate(LocalDateTime.now()) + "):");
        messageReserve.setReservationDate(UserIO.readDate(true));
        System.out.print("Idő kerekítve: " + UserIO.printDate(messageReserve.getReservationDate()));

        // Foglalás adatainak ellenőrzése, előfoglalás
        System.out.print("\nFoglalási adatok ellenőrzése.. ");
        oos.writeObject(messageReserve);
        messageReserve = (MessageReserve) ois.readObject();
        if (messageReserve.isApproved()) {
            System.out.print("Foglalási adatok elfogadva!");
        }
        else {
            System.out.print("Foglalási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + messageReserve.getTransactionMessage());
        }

        // TODO: Foglalás jóváhagyása, vagy törlése
    }

    // Beszállítás (megérkezett a szállítmány)
    static void startReceiving(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // Beérkezés adatainak bekérése
        System.out.print("Tranzakció azonosító: ");         // Munkalap sorszáma
        int transactionID = Integer.parseInt(br.readLine());
        System.out.print("Beérkezés időpontja: ");          // TODO: a dátumot csak a teszt célból kell megadni, realtime működés esetén LocalDateTime.now() kell ide
        LocalDateTime receivingDate = UserIO.readDate(false);
        MessageActivate messageActivate = new MessageActivate(transactionID, receivingDate);
        oos.writeObject(messageActivate);
        messageActivate = (MessageActivate) ois.readObject();
        if (messageActivate.isApproved()){
            System.out.print("Beszállítási adatok elfogadva - munkalap aktiválva!");
        } else {
            System.out.print("Beszállítási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + messageActivate.getTransactionMessage());
        }
    }

    // Új Kiszállítás
    static void newDelivery(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Új munkalap (tranzakció azonosító) kérelem
        MessageCreate messageCreate = new MessageCreate();
        oos.writeObject(messageCreate);
        messageCreate = (MessageCreate) ois.readObject();

        // Új rendelés (kiszállítási kérelem)
        MessageOrder messageOrder = new MessageOrder();
        messageOrder.setTransactionID(messageCreate.getTransactionID());
        System.out.print("\nÚj munkalap létrehozva - Tranzakcióazonosító: " + messageCreate.getTransactionID());

        // Rendelés adatainak bekérése
        System.out.print("\nVevőkód: ");
        messageOrder.setRenterID(br.readLine());
        System.out.print("Cikkszám: ");
        messageOrder.setPartNumber(br.readLine());
        System.out.print("Raklapok száma: ");
        messageOrder.setPallets(Integer.parseInt(br.readLine()));
        System.out.println("Szállítás időpontja (Példa: " + UserIO.printDate(LocalDateTime.now()) + "):");
        messageOrder.setReservationDate(UserIO.readDate(true));
        System.out.print("Idő kerekítve: " + UserIO.printDate(messageOrder.getReservationDate()));

        // Rendelés adatainak ellenőrzése, előfoglalás
        System.out.print("\nSzállítási adatok ellenőrzése.. ");
        oos.writeObject(messageOrder);
        messageOrder = (MessageOrder) ois.readObject();
        if (messageOrder.isApproved()) {
            System.out.print("Szállítási adatok elfogadva!");
        }
        else {
            System.out.print("Szállítási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + messageOrder.getTransactionMessage());
        }

        // TODO: Szállítás jóváhagyása, vagy törlése
    }

    // TODO: Kifejtendő - Kiszállítás (megérkezett a szállító)
    static void startDelivery(ObjectOutputStream oos, ObjectInputStream ois) {
        // TODO 1: dátumot csekkolni - ha nem egyezik a foglalással akkor kuka
        // TODO 2: munkalapot aktiválni kell, hogy lássa a raktáros (raktáros kiszállítás funkció is kell)
    }

}
