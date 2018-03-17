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

        // Tranzakciószám kérése a szervertől
        Worksheet worksheet = new Worksheet(Worksheet.WorkType.Incoming);
        worksheet.setTransaction(Worksheet.TransactionType.Initialize);
        oos.writeObject(worksheet);
        worksheet = (Worksheet)ois.readObject();
        System.out.print("\nÚj munkalap létrehozva - Tranzakcióazonosító: " +
                worksheet.getTransactionID() + " (isInitialized: " + worksheet.isInitialized() + ")");

        // Kérelem adatainak bekérése a munkalapra
        Reservation reservation = new Reservation();
        System.out.print("\nVevőkód: ");
        reservation.RenterID = br.readLine();
        System.out.print("Cikkszám: ");
        reservation.PartNumber = br.readLine();
        System.out.print("Hűtött áru? (i/n): ");
        reservation.IsCooled = UserIO.readBoolean();
        System.out.print("Raklapok száma: ");
        reservation.Pallets = Integer.parseInt(br.readLine());
        System.out.println("Foglalás időpontja (Példa: " + UserIO.printDate(LocalDateTime.now()) + "):");
        reservation.ReservationDate = UserIO.readDate(true);
        System.out.print("Idő kerekítve: " + UserIO.printDate(worksheet.getReservedDate()));

        UserIO.fillWorkSheet(worksheet,reservation);

        // Foglalás adatainak ellenőrzése, előfoglalás
        System.out.print("\nFoglalási adatok ellenőrzése.. ");
        worksheet.setTransaction(Worksheet.TransactionType.Approve);
        oos.writeObject(worksheet);
        worksheet = (Worksheet)ois.readObject();
        if (worksheet.isApproved()) {
            System.out.print("Foglalási adatok elfogadva!");
        }
        else {
            System.out.print("Foglalási adatok elutasítva!");
            System.out.print("\nSzerver üzenete: " + worksheet.getTransactionMessage());
        }

        // TODO: Foglalás jóváhagyása, vagy törlése
    }

    // TODO: Kifejtendő -
    static void doReceiving(ObjectOutputStream oos, ObjectInputStream ois) {

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
