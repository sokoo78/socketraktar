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
        List<ReservationMessage> testReservationMessages = new ArrayList<>();

        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse("2019-12-01 12:00", dateformat);

        // Sikeres foglalások
        testReservationMessages.add(new ReservationMessage("BEBE", "CIKK75110001", false, 1, dateTime));
        testReservationMessages.add(new ReservationMessage("REZO", "NORM65110010", false, 1, dateTime));
        testReservationMessages.add(new ReservationMessage("BEBE", "CIKK75110002", false, 2, dateTime));
        testReservationMessages.add(new ReservationMessage("REZO", "HUTO75110011", true,  1, dateTime));
        testReservationMessages.add(new ReservationMessage("GAPE", "NORM65110010", true,  1, dateTime));
        testReservationMessages.add(new ReservationMessage("GAPE", "NORM65110011", true,  1, dateTime));
        testReservationMessages.add(new ReservationMessage("GAPE", "NORM65110012", true,  1, dateTime.plusDays(2)));

        // Sikertelen foglalások
        // 1. Bérlő nem létezik
        testReservationMessages.add(new ReservationMessage("NUKU", "CIKK75110001", false, 5, dateTime.plusDays(1)));
        // 2. Nincs a bérlőnek hűtött lokációja
        testReservationMessages.add(new ReservationMessage("BEBE", "CIKK75110001", true,  1, dateTime.plusDays(1)));
        // 3. Nincs a bérlőnek elég szabad helye
        testReservationMessages.add(new ReservationMessage("BEBE", "CIKK75110001", false, 501, dateTime.plusDays(1)));
        // 4. Nincs a megadott időpontban szabad terminál
        testReservationMessages.add(new ReservationMessage("GAPE", "NORM65110010", true,  1, dateTime));

        // Tesztek futtatása
        for (int i = 0; i < testReservationMessages.size(); i++) {
            // Inicializálás
            ReservationMessage reservationMessage;
            reservationMessage = testReservationMessages.get(i);
            reservationMessage.setWorkSheetType(Worksheet.WorkSheetType.Incoming);
            oos.writeObject(reservationMessage);
            reservationMessage = (ReservationMessage) ois.readObject();

            // Jóváhagyás
            oos.writeObject(reservationMessage);
            reservationMessage = (ReservationMessage) ois.readObject();

            // Tömör kiírás
            UserIO.printReservation(reservationMessage);
        }
    }

    // Beérkezési teszt
    public static void doReceivingTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        System.out.println("\n Beérkezési teszt\n");

        List<ReceivingMessage> testReceivingMessages = new ArrayList<>();
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Sikeres beérkezések
        testReceivingMessages.add(new ReceivingMessage(1,LocalDateTime.parse("2019-12-01 12:00", dateformat))); // Pont időben
        testReceivingMessages.add(new ReceivingMessage(2,LocalDateTime.parse("2019-12-01 12:15", dateformat))); // Intervallumon belül
        testReceivingMessages.add(new ReceivingMessage(3,LocalDateTime.parse("2019-12-01 12:30", dateformat))); // Utolsó pillanatban

        // Sikertelen beérkezések
        testReceivingMessages.add(new ReceivingMessage(0,LocalDateTime.parse("2019-12-01 11:59", dateformat))); // Nem létező tranzakció
        testReceivingMessages.add(new ReceivingMessage(1,LocalDateTime.parse("2019-12-01 12:00", dateformat))); // Már aktív
        testReceivingMessages.add(new ReceivingMessage(4,LocalDateTime.parse("2019-12-01 11:59", dateformat))); // Egy perccel korábban
        testReceivingMessages.add(new ReceivingMessage(5,LocalDateTime.parse("2019-12-01 12:31", dateformat))); // Egy perccel később

        // Tesztek futtatása
        for (int i = 0; i < testReceivingMessages.size(); i++) {
            ReceivingMessage receivingMessage = testReceivingMessages.get(i);
            oos.writeObject(receivingMessage);
            receivingMessage = (ReceivingMessage) ois.readObject();
            if (receivingMessage.isApproved()){
                System.out.print("\n" + receivingMessage.getTransactionID() + " OK");
            } else {
                System.out.print("\n" + receivingMessage.getTransactionID() + " NOK: " + receivingMessage.getTransactionMessage() + "\t");
            }
        }
    }

    public static void doIncomingTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        System.out.println("\n Bevételezés teszt\n");

        // Adatok lekérése a szerverről az 1-es teszt tranzakcióhoz
        ReceivingMessage receivingMessage = new ReceivingMessage(1);
        receivingMessage.setApproved();
        oos.writeObject(receivingMessage);
        receivingMessage = (ReceivingMessage)ois.readObject();
        if (receivingMessage.isProcessing()){
            System.out.print("\nAdatlekérés OK");
        } else {
            System.out.print("\nAdatlekérés NOK - Szerver üzenete: " + receivingMessage.getTransactionMessage());
        }

        // Paletták szkennelése és kipakolása
        for (int i = 0; i < receivingMessage.getPallets(); i++) {
            receivingMessage.setInternalPartNumber(generateInternalPartNumber(receivingMessage));
            UnloadingMessage unloadingMessage = new UnloadingMessage(receivingMessage.getTransactionID(), receivingMessage.getInternalPartNumber());
            oos.writeObject(unloadingMessage);
            unloadingMessage = (UnloadingMessage)ois.readObject();
            if (unloadingMessage.isConfirmed()){
                System.out.print("\nKirakodás OK " + receivingMessage.getTerminalID() + " terminálra!");
            } else {
                System.out.print("\nKirakodás NOK - Szerver üzenete: " + receivingMessage.getTransactionMessage());
            }
        }

        // Munkalap lejelentése a szervernek
        receivingMessage.setUnloaded();
        oos.writeObject(receivingMessage);
        receivingMessage = (ReceivingMessage)ois.readObject();
        if (receivingMessage.isCompleted()){
            System.out.print("\nBevételezés OK");
        } else {
            System.out.print("\nBevételezés NOK - Szerver üzenete: " + receivingMessage.getTransactionMessage());
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
