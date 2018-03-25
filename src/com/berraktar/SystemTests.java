package com.berraktar;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.berraktar.Storekeeper.generateInternalPartNumber;

public final class SystemTests {

    // Foglalási teszt
    public static void doReservationTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        System.out.println("\n Foglalási teszt\n");

        // Teszt foglalási adatok
        List<MessageReserve> testMessageReserves = new ArrayList<>();

        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse("2019-12-01 12:00", dateformat);

        // Sikeres foglalások
        testMessageReserves.add(new MessageReserve("BEBE", "CIKK75110001", false, 1, dateTime));
        testMessageReserves.add(new MessageReserve("REZO", "NORM65110010", false, 1, dateTime));
        testMessageReserves.add(new MessageReserve("BEBE", "CIKK75110002", false, 2, dateTime));
        testMessageReserves.add(new MessageReserve("REZO", "HUTO75110011", true,  1, dateTime));
        testMessageReserves.add(new MessageReserve("GAPE", "NORM65110010", true,  1, dateTime));
        testMessageReserves.add(new MessageReserve("GAPE", "NORM65110011", true,  1, dateTime));
        testMessageReserves.add(new MessageReserve("GAPE", "NORM65110012", true,  1, dateTime.plusDays(2)));

        // Sikertelen foglalások
        // 1. Bérlő nem létezik
        testMessageReserves.add(new MessageReserve("NUKU", "CIKK75110001", false, 5, dateTime.plusDays(1)));
        // 2. Nincs a bérlőnek hűtött lokációja
        testMessageReserves.add(new MessageReserve("BEBE", "CIKK75110001", true,  1, dateTime.plusDays(1)));
        // 3. Nincs a bérlőnek elég szabad helye
        testMessageReserves.add(new MessageReserve("BEBE", "CIKK75110001", false, 501, dateTime.plusDays(1)));
        // 4. Nincs a megadott időpontban szabad terminál
        testMessageReserves.add(new MessageReserve("GAPE", "NORM65110010", true,  1, dateTime));

        // Tesztek futtatása
        for (int i = 0; i < testMessageReserves.size(); i++) {

            // Tranzakció azonosító kérés
            MessageCreate messageCreate = new MessageCreate();
            messageCreate.setIncoming();
            oos.writeObject(messageCreate);
            messageCreate = (MessageCreate) ois.readObject();

            // Foglalási kérelem
            MessageReserve messageReserve = testMessageReserves.get(i);
            messageReserve.setTransactionID(messageCreate.getTransactionID());
            messageReserve.setWorkSheetType(Worksheet.WorkSheetType.Incoming);
            oos.writeObject(messageReserve);
            messageReserve = (MessageReserve) ois.readObject();

            // Tömör kiírás
            UserIO.printReservation(messageReserve);
        }
    }

    // Beérkezési teszt
    public static void doActivationTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        System.out.println("\n Beérkezési teszt\n");

        List<MessageActivate> testActivations = new ArrayList<>();
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Sikeres beérkezések
        testActivations.add(new MessageActivate(1,LocalDateTime.parse("2019-12-01 12:00", dateformat))); // Pont időben
        testActivations.add(new MessageActivate(2,LocalDateTime.parse("2019-12-01 12:15", dateformat))); // Intervallumon belül
        testActivations.add(new MessageActivate(3,LocalDateTime.parse("2019-12-01 12:30", dateformat))); // Utolsó pillanatban

        // Sikertelen beérkezések
        testActivations.add(new MessageActivate(0,LocalDateTime.parse("2019-12-01 11:59", dateformat))); // Nem létező tranzakció
        testActivations.add(new MessageActivate(1,LocalDateTime.parse("2019-12-01 12:00", dateformat))); // Már aktív
        testActivations.add(new MessageActivate(4,LocalDateTime.parse("2019-12-01 11:59", dateformat))); // Egy perccel korábban
        testActivations.add(new MessageActivate(5,LocalDateTime.parse("2019-12-01 12:31", dateformat))); // Egy perccel később

        // Tesztek futtatása
        for (int i = 0; i < testActivations.size(); i++) {
            MessageActivate messageActivate = testActivations.get(i);
            oos.writeObject(messageActivate);
            messageActivate = (MessageActivate) ois.readObject();
            if (messageActivate.isApproved()){
                System.out.print("\n" + messageActivate.getTransactionID() + " OK");
            } else {
                System.out.print("\n" + messageActivate.getTransactionID() + " NOK: " + messageActivate.getTransactionMessage() + "\t");
            }
        }
    }

    // Betárolási teszt
    public static void doProcessingTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        System.out.println("\n Bevételezés teszt\n");

        // Adatok lekérése a szerverről az 1-es teszt tranzakcióhoz
        MessageProcess messageProcess = new MessageProcess(1);
        messageProcess.setApproved();
        oos.writeObject(messageProcess);
        messageProcess = (MessageProcess)ois.readObject();
        if (messageProcess.isApproved()){
            System.out.print("\nAdatlekérés OK");
        } else {
            System.out.print("\nAdatlekérés NOK - Szerver üzenete: " + messageProcess.getTransactionMessage());
        }

        // Paletták szkennelése és kipakolása
        for (int i = 0; i < messageProcess.getPallets(); i++) {
            messageProcess.setInternalPartNumber(generateInternalPartNumber(messageProcess));
            MessageUnload messageUnload = new MessageUnload(messageProcess.getTransactionID(), messageProcess.getInternalPartNumber());
            messageUnload.setTerminalID(messageProcess.getTerminalID());
            oos.writeObject(messageUnload);
            messageUnload = (MessageUnload)ois.readObject();
            if (messageUnload.isApproved()){
                System.out.print("\nKirakodás OK " + messageUnload.getTerminalID() + " terminálra!");
            } else {
                System.out.print("\nKirakodás NOK - Szerver üzenete: " + messageUnload.getTransactionMessage());
            }
        }

        // Munkalap lejelentése a szervernek
        MessageComplete messageComplete = new MessageComplete(messageProcess.getTransactionID());
        messageComplete.setRenterID(messageProcess.getRenterID());
        messageComplete.setInternalPartNumber(messageProcess.getInternalPartNumber());
        oos.writeObject(messageComplete);
        messageComplete = (MessageComplete)ois.readObject();
        if (messageComplete.isApproved()){
            System.out.print("\nBevételezés OK");
        } else {
            System.out.print("\nBevételezés NOK - Szerver üzenete: " + messageComplete.getTransactionMessage());
        }
    }

    // Kliens-Szerver kommunikáció teszt
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
