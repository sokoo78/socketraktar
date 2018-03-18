package com.berraktar;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.berraktar.Storekeeper.generateInternalPartNumber;

public final class SystemTests {

    // Foglalási teszt
    public static void doNewReservationTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        System.out.println("\n Foglalási teszt\n");

        // Teszt foglalási adatok
        List<Reservation> testReservations = new ArrayList<>();

        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse("2019-12-01 12:00", dateformat);

        // Sikeres foglalások
        testReservations.add(new Reservation("BEBE", "CIKK75110001", false, 1, dateTime));
        testReservations.add(new Reservation("REZO", "NORM65110010", false, 1, dateTime));
        testReservations.add(new Reservation("BEBE", "CIKK75110002", false, 2, dateTime));
        testReservations.add(new Reservation("REZO", "HUTO75110011", true,  1, dateTime));
        testReservations.add(new Reservation("GAPE", "NORM65110010", true,  1, dateTime));
        testReservations.add(new Reservation("GAPE", "NORM65110011", true,  1, dateTime));
        testReservations.add(new Reservation("GAPE", "NORM65110012", true,  1, dateTime.plusDays(2)));

        // Sikertelen foglalások
        // 1. Bérlő nem létezik
        testReservations.add(new Reservation("NUKU", "CIKK75110001", false, 5, dateTime.plusDays(1)));
        // 2. Nincs a bérlőnek hűtött lokációja
        testReservations.add(new Reservation("BEBE", "CIKK75110001", true,  1, dateTime.plusDays(1)));
        // 3. Nincs a bérlőnek elég szabad helye
        testReservations.add(new Reservation("BEBE", "CIKK75110001", false, 501, dateTime.plusDays(1)));
        // 4. Nincs a megadott időpontban szabad terminál
        testReservations.add(new Reservation("GAPE", "NORM65110010", true,  1, dateTime));

        // Tesztek futtatása
        for (int i = 0; i < testReservations.size(); i++) {
            // Inicializálás
            Reservation reservation;
            reservation = testReservations.get(i);
            reservation.setWorkSheetType(Worksheet.WorkSheetType.Incoming);
            oos.writeObject(reservation);
            reservation = (Reservation) ois.readObject();

            // Jóváhagyás
            oos.writeObject(reservation);
            reservation = (Reservation) ois.readObject();

            // Tömör kiírás
            UserIO.printReservation(reservation);
        }
    }

    // Beérkezési teszt
    public static void doReceivingTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        System.out.println("\n Beérkezési teszt\n");

        List<Receiving> testReceivings = new ArrayList<>();
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Sikeres beérkezések
        testReceivings.add(new Receiving(1,LocalDateTime.parse("2019-12-01 12:00", dateformat))); // Pont időben
        testReceivings.add(new Receiving(2,LocalDateTime.parse("2019-12-01 12:15", dateformat))); // Intervallumon belül
        testReceivings.add(new Receiving(3,LocalDateTime.parse("2019-12-01 12:30", dateformat))); // Utolsó pillanatban

        // Sikertelen beérkezések
        testReceivings.add(new Receiving(0,LocalDateTime.parse("2019-12-01 11:59", dateformat))); // Nem létező tranzakció
        testReceivings.add(new Receiving(1,LocalDateTime.parse("2019-12-01 12:00", dateformat))); // Már aktív
        testReceivings.add(new Receiving(4,LocalDateTime.parse("2019-12-01 11:59", dateformat))); // Egy perccel korábban
        testReceivings.add(new Receiving(5,LocalDateTime.parse("2019-12-01 12:31", dateformat))); // Egy perccel később

        // Tesztek futtatása
        for (int i = 0; i < testReceivings.size(); i++) {
            Receiving receiving = testReceivings.get(i);
            oos.writeObject(receiving);
            receiving = (Receiving) ois.readObject();
            if (receiving.isApproved()){
                System.out.print("\n" + receiving.getTransactionID() + " OK");
            } else {
                System.out.print("\n" + receiving.getTransactionID() + " NOK: " +receiving.getTransactionMessage() + "\t");
            }
        }
    }

    public static void doIncomingTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        System.out.println("\n Bevételezés teszt\n");

        // Adatok lekérése a szerverről az 1-es teszt tranzakcióhoz
        Receiving receiving = new Receiving(1);
        receiving.setApproved();
        oos.writeObject(receiving);
        receiving = (Receiving)ois.readObject();
        if (receiving.isProcessing()){
            System.out.print("\nAdatlekérés OK");
        } else {
            System.out.print("\nAdatlekérés NOK - Szerver üzenete: " + receiving.getTransactionMessage());
        }

        // Paletták szkennelése és kipakolása
        for (int i = 0; i < receiving.getPallets(); i++) {
            receiving.setInternalPartNumber(generateInternalPartNumber(receiving));
            Unloading unloading = new Unloading(receiving.getTransactionID(), receiving.getInternalPartNumber());
            oos.writeObject(unloading);
            unloading = (Unloading)ois.readObject();
            if (unloading.isConfirmed()){
                System.out.print("\nKirakodás OK " + receiving.getTerminalID() + " terminálra!");
            } else {
                System.out.print("\nKirakodás NOK - Szerver üzenete: " + receiving.getTransactionMessage());
            }
        }

        // Munkalap lejelentése a szervernek
        receiving.setUnloaded();
        oos.writeObject(receiving);
        receiving = (Receiving)ois.readObject();
        if (receiving.isCompleted()){
            System.out.print("\nBevételezés OK");
        } else {
            System.out.print("\nBevételezés NOK - Szerver üzenete: " + receiving.getTransactionMessage());
        }
    }

    // Szerver teszt
    static void doServerTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Integer firstNumber = null;
        Integer secondNumber = null;
        try{
            System.out.print("Első szám:");
            firstNumber = Integer.parseInt(br.readLine());
            System.out.print("Második szám:");
            secondNumber = Integer.parseInt(br.readLine());
        } catch(NumberFormatException nfe){
            System.err.println("Érvénytelen formátum!");
        }

        // Adat küldése a szerverre az OutpuStream-en keresztül
        // Bármilyen objektum lehet ami implementálja a Serializable interfészt
        ServerTest serverTest = new ServerTest(firstNumber,secondNumber);
        oos.writeObject(serverTest);
        // Szerver válaszának a kiolvasása az InputStream-ről
        ServerTest returnServerTest = (ServerTest)ois.readObject();
        System.out.println("Eredmény: " + returnServerTest.getResult());
    }

}
