package com.berraktar;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class SystemTests {

    // Foglalási teszt
    public static void doNewReservationTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {

        // Teszt foglalási adatok
        List<Reservation> testReservations = new ArrayList<>();
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse("2019-12-01 12:00", dateformat);

        // Sikeres foglalások
        testReservations.add(new Reservation("BEBE", "CIKK75110001", false, 1, LocalDateTime.now().plusDays(1)));
        testReservations.add(new Reservation("REZO", "NORM65110010", false, 1, LocalDateTime.now().plusDays(1)));
        testReservations.add(new Reservation("BEBE", "CIKK75110002", false, 2, LocalDateTime.now().plusDays(1)));
        testReservations.add(new Reservation("REZO", "HUTO75110011", true,  1, LocalDateTime.now().plusDays(1)));
        testReservations.add(new Reservation("GAPE", "NORM65110010", true,  1, dateTime));
        testReservations.add(new Reservation("GAPE", "NORM65110011", true,  1, dateTime));
        testReservations.add(new Reservation("GAPE", "NORM65110012", true,  1, dateTime));

        // Sikertelen foglalások
        // 1. Bérlő nem létezik
        testReservations.add(new Reservation("NUKU", "CIKK75110001", false, 5, LocalDateTime.now().plusDays(1)));
        // 2. Nincs a bérlőnek hűtött lokációja
        testReservations.add(new Reservation("BEBE", "CIKK75110001", true,  1, LocalDateTime.now().plusDays(1)));
        // 3. Nincs a bérlőnek elég szabad helye
        testReservations.add(new Reservation("BEBE", "CIKK75110001", false, 501, LocalDateTime.now().plusDays(1)));
        // 4. Nincs a megadott időpontban szabad terminál
        testReservations.add(new Reservation("GAPE", "NORM65110010", true,  1, dateTime));

        // Inicializálás
        for (int i = 0; i < testReservations.size(); i++) {
            Worksheet worksheet = new Worksheet(Worksheet.WorkType.Incoming);
            worksheet.setTransaction(Worksheet.TransactionType.Initialize);
            oos.writeObject(worksheet);
            worksheet = (Worksheet) ois.readObject();

            // Kitöltés
            Reservation reservation = testReservations.get(i);
            worksheet = UserIO.fillWorkSheet(worksheet, reservation);

            // Jóváhagyás
            worksheet.setTransaction(Worksheet.TransactionType.Approve);
            oos.writeObject(worksheet);
            worksheet = (Worksheet) ois.readObject();

            // Tömör kiírás
            UserIO.printWorksheet(worksheet);
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
