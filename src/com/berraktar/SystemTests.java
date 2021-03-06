package com.berraktar;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.berraktar.Storekeeper.generateInternalPartNumber;

final class SystemTests {

    // Teszt menü
    private final static String menu = "\nBérraktár tesztek menü:\n\t" +
            "1. Foglalás teszt\n\t" +
            "2. Beérkezés teszt\n\t" +
            "3. Bevételezés teszt\n\t" +
            "4. Rendelés teszt\n\t" +
            "5. Kiszállítás teszt\n\t" +
            "6. Jegyzőkönyv teszt\n\t" +
            "7. Szerver teszt\n\t" +
            "8. Kilépés\n" +
            "Válassz menüpontot: ";

    // Teszt menü
    static void ShowMenu(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(menu);
        String input = br.readLine();
        while(!input.equals("8")){
            switch(input){
                case "1":
                    SystemTests.ReservationTest(oos, ois);
                    break;
                case "2":
                    SystemTests.ActivationTest(oos, ois);
                    break;
                case "3":
                    SystemTests.ProcessingTest(oos, ois);
                    break;
                case "4":
                    SystemTests.OrderingTest(oos, ois);
                    break;
                case "5":
                    SystemTests.doShippingTest(oos, ois);
                    break;
                case "6":
                    SystemTests.doProtocolTest(oos, ois);
                    break;
                case "7":
                    SystemTests.doServerTest(oos, ois);
                    break;
                default:
                    System.out.println("A megadott menüpont nem létezik! (" + input + ")");
            }
            System.out.print("\nNyomj ENTER-t a folytatáshoz!");
            System.in.read();
            System.out.print(menu);
            input = br.readLine();
        }
    }

    // Foglalási teszt
    private static void ReservationTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
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
        for (MessageReserve testMessageReserve : testMessageReserves) {

            // Tranzakció azonosító kérés
            MessageCreate messageCreate = new MessageCreate();
            messageCreate.setIncoming();
            oos.writeObject(messageCreate);
            messageCreate = (MessageCreate) ois.readObject();

            // Foglalási kérelem
            MessageReserve messageReserve = testMessageReserve;
            messageReserve.setTransactionID(messageCreate.getTransactionID());
            oos.writeObject(messageReserve);
            messageReserve = (MessageReserve) ois.readObject();

            // Tömör kiírás
            UserIO.printReservation(messageReserve);
        }
    }

    // Beérkezési teszt
    private static void ActivationTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
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
        for (MessageActivate messageActivate : testActivations) {
            oos.writeObject(messageActivate);
            messageActivate = (MessageActivate) ois.readObject();
            if (messageActivate.isApproved()) {
                System.out.print("\n" + messageActivate.getTransactionID() + " OK");
            } else {
                System.out.print("\n" + messageActivate.getTransactionID() + " NOK: " + messageActivate.getTransactionMessage() + "\t");
            }
        }
    }

    // Betárolási teszt
    private static void ProcessingTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
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
            messageProcess.setInternalPartNumber(generateInternalPartNumber(messageProcess, i + 1));
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
        MessageStore messageStore = new MessageStore(messageProcess.getTransactionID());
        messageStore.setRenterID(messageProcess.getRenterID());
        messageStore.setInternalPartNumber(messageProcess.getInternalPartNumber());
        oos.writeObject(messageStore);
        messageStore = (MessageStore)ois.readObject();
        if (messageStore.isApproved()){
            System.out.print("\nBevételezés OK");
        } else {
            System.out.print("\nBevételezés NOK - Szerver üzenete: " + messageStore.getTransactionMessage());
        }
    }

    // Rendelési teszt
    private static void OrderingTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        System.out.println("\n Rendelési teszt\n");

        // Teszt foglalási adatok
        List<MessageOrder> testMessageOrders = new ArrayList<>();
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse("2019-12-01 12:00", dateformat);

        // Sikeres rendelés
        testMessageOrders.add(new MessageOrder("BEBE", "CIKK75110001", 1, dateTime.plusDays(3)));

        // Sikertelen rendelések
        // 1. Bérlő nem létezik
        testMessageOrders.add(new MessageOrder("NUKU", "CIKK75110001", 1, dateTime));
        // 2. Nincs elég a kért cikkből
        testMessageOrders.add(new MessageOrder("BEBE", "CIKK75110001", 2, dateTime));
        // 3. Nincs a megadott időpontban szabad terminál - TODO ehhez még kell generálni ordereket
        //testMessageOrders.add(new MessageOrder("BEBE", "CIKK75110001", 1, dateTime));

        // Tesztek futtatása
        for (MessageOrder testMessageOrder : testMessageOrders) {

            // Tranzakció azonosító kérés
            MessageCreate messageCreate = new MessageCreate();
            oos.writeObject(messageCreate);
            messageCreate = (MessageCreate) ois.readObject();

            // Foglalási kérelem
            MessageOrder messageOrder = testMessageOrder;
            messageOrder.setTransactionID(messageCreate.getTransactionID());
            oos.writeObject(messageOrder);
            messageOrder = (MessageOrder) ois.readObject();

            // Tömör kiírás
            UserIO.printOrdering(messageOrder);
        }
    }

    // Kiszállítási teszt
    private static void doShippingTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {

        // Munkalap aktiválása
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        MessageActivate messageActivate = new MessageActivate(12,LocalDateTime.parse("2019-12-04 12:15", dateformat));
        oos.writeObject(messageActivate);
        messageActivate = (MessageActivate) ois.readObject();
        if (messageActivate.isApproved()) {
            System.out.print("Aktiválás OK");
        } else {
            System.out.print("Aktiválás NOK: " + messageActivate.getTransactionMessage() + "\t");
        }

        // Munkalap adatok lekérése a szerverről
        MessageProcess messageProcess = new MessageProcess(12);
        oos.writeObject(messageProcess);
        messageProcess = (MessageProcess)ois.readObject();
        if (messageProcess.isApproved()){
            System.out.print("\nAdatlekérés OK");
        } else {
            System.out.print("\nAdatlekérés NOK: " + messageProcess.getTransactionMessage());
            return;
        }

        // Paletta lejelentése a szervernek - kirakás a terminálra
        MessageLoad messageLoad = new MessageLoad(messageProcess.getTransactionID());
        oos.writeObject(messageLoad);
        messageLoad = (MessageLoad)ois.readObject();
        if (messageLoad.isApproved()){
            System.out.print("\nKitárolás OK - Terminál: " + messageProcess.getTerminalID());
        } else {
            System.out.print("\nKitárolás NOK: " + messageLoad.getTransactionMessage());
            return;
        }

        // Munkalap lejelentése a szervernek - kiszállítás a terminálról
        MessageShip messageShip = new MessageShip(messageProcess.getTransactionID());
        oos.writeObject(messageShip);
        messageShip = (MessageShip)ois.readObject();
        if (messageShip.isApproved()){
            System.out.print("\nKiszállítás OK - munkalap lezárva!");
        } else {
            System.out.print("\nKiszállítás NOK: " + messageShip.getTransactionMessage());
        }
    }

    // Jegyzőkönyv teszt
    private static void doProtocolTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Protocol protocol = new Protocol(1, LocalDateTime.now());
        protocol.setTransactionMessage("Teszt jegyzőkönyv szöveg - a pisztácia elfogyott, csokoládé nem is volt!");
        oos.writeObject(protocol);
        protocol = (Protocol)ois.readObject();
        if (protocol.isApproved()) {
            System.out.print("OK");
        } else {
            System.out.print("NOK - Szerver üzenete: " + protocol.getTransactionMessage());
        }
    }

    // Kliens-Szerver kommunikáció teszt
    private static void doServerTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
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
        MessageServerTest messageServerTest = new MessageServerTest(firstNumber,secondNumber);
        oos.writeObject(messageServerTest);
        // Szerver válaszának a kiolvasása az InputStream-ről
        MessageServerTest returnMessageServerTest = (MessageServerTest)ois.readObject();
        System.out.println("Eredmény: " + returnMessageServerTest.getResult());
    }

}
