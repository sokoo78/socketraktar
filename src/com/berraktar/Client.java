package com.berraktar;

// Socket kliens

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Client {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Socket kliens indítása és kapcsolódás a szerverhez
        Socket socket = new Socket("localhost", Server.PORT);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

        // Bejelentkezés
        Employee employee = DoBejelentkezes(objectInputStream, objectOutputStream);

        // Menü megjelenítése
        showMenu(employee.getPositionID(), objectOutputStream, objectInputStream);

        // Kapcsolat bontása
        socket.close();
    }

    private static void showMenu(int positionID, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Diszpécser menü
        if (positionID == 1){
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("\nBérraktár diszpécser menü:\n\t" +
                    "1. Új Foglalás\n\t" +
                    "2. Beszállítás\n\t" +
                    "3. Új kiszállítás\n\t" +
                    "4. Kiszállítás\n\t" +
                    "5. Foglalás teszt\n\t" +
                    "6. Szerver teszt\n\t" +
                    "7. Kilépés\n" +
                    "Válassz menüpontot: ");
            String input = br.readLine();
            while(!input.equals("7")){
                switch(input){
                    case "1":
                        doFoglalas(oos, ois);
                        break;
                    case "2":
                        doBeszallitas(oos, ois);
                        break;
                    case "3":
                        doUjKiszallitas(oos, ois);
                        break;
                    case "4":
                        doKiszallitas(oos, ois);
                        break;
                    case "5":
                        doFoglalasTest(oos, ois);
                        break;
                    case "6":
                        doTest(oos, ois);
                        break;
                    default:
                        System.out.println("A megadott menüpont nem létezik! (" + input + ")");
                }
                System.out.print("\nNyomj ENTER-t a folytatáshoz!");
                System.in.read();
                System.out.print("\nBérraktár diszpécser menü:\n\t" +
                        "1. Új Foglalás\n\t" +
                        "2. Beszállítás\n\t" +
                        "3. Új kiszállítás\n\t" +
                        "4. Kiszállítás\n\t" +
                        "5. Foglalás teszt\n\t" +
                        "6. Szerver teszt\n\t" +
                        "7. Kilépés\n" +
                        "Válassz menüpontot: ");
                input = br.readLine();
            }
        }
        // TODO: Raktáros menü
        // Diszpécser menü
        if (positionID == 3){
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("\nBérraktár könyvelés menü:\n\t" +
                    "1. Ügyfél lista\n\t" +
                    "2. Munkalap lista\n\t" +
                    "3. Lokáció lista\n\t" +
                    "4. Terminál foglalási lista\n\t" +
                    "5. Kilépés\n" +
                    "Válassz menüpontot: ");
            String input = br.readLine();
            while(!input.equals("5")){
                switch(input){
                    case "1":
                        GetReport(oos, ois, Report.ReportType.Renters);
                        break;
                    case "2":
                        GetReport(oos, ois, Report.ReportType.Worksheets);
                        break;
                    case "3":
                        GetReport(oos, ois, Report.ReportType.Locations);
                        break;
                    default:
                        System.out.println("A megadott menüpont nem létezik! (" + input + ")");
                }
                System.out.print("\nNyomj ENTER-t a folytatáshoz!");
                System.in.read();
                System.out.print("\nBérraktár könyvelés menü:\n\t" +
                        "1. Ügyfél lista\n\t" +
                        "2. Munkalap lista\n\t" +
                        "3. Lokáció lista\n\t" +
                        "4. Terminál foglalási lista\n\t" +
                        "5. Kilépés\n" +
                        "Válassz menüpontot: ");
                input = br.readLine();
            }
        }
    }

    private static void GetReport(ObjectOutputStream oos, ObjectInputStream ois, Report.ReportType reportType) throws IOException, ClassNotFoundException {
        Report report = new Report(reportType);
        oos.writeObject(report);
        report = (Report) ois.readObject();
        System.out.println(report.getReply());
    }

    // TODO: Buta login, ellenőrzést és visszatérést betenni
    private static Employee DoBejelentkezes(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Felhasználó:");
        String userName = br.readLine();
        System.out.print("Beosztás (1 - diszpécser, 2 - raktáros, 3 - könyvelő):");
        int userPosition = Integer.parseInt(br.readLine())-1;
        // Név és pozíció küldése a szervernek
        Employee employee = new Employee(userName, Employee.UserType.values()[userPosition]);
        oos.writeObject(employee);
        // Bejelentkezés ellenőrzése
        employee = (Employee) ois.readObject();
        if (employee.isLoggedin()){
            System.out.print("Sikeres bejelentkezés: " + employee.getName() + ", " + employee.getPosition());
        } else {
            System.out.print("Sikertelen bejelentkezés!");
        }
        return employee;
    }

    private static void doFoglalas(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Tranzakciószám kérése a szervertől
        Worksheet worksheet = new Worksheet(Worksheet.WorkType.Incoming);
        worksheet.setTransaction(Worksheet.TransactionType.Initialize);
        oos.writeObject(worksheet);
        worksheet = (Worksheet)ois.readObject();

        // Kérelem adatainak bekérése a munkalapra
        System.out.print("\nÚj munkalap létrehozva - Tranzakcióazonosító: " + worksheet.getTransactionID() + " (isInitialized: " + worksheet.isInitialized() + ")");
        System.out.print("\nVevőkód: ");
        worksheet.setRenterID(br.readLine());
        System.out.print("Cikkszám: ");
        worksheet.setExternalID(br.readLine());
        System.out.print("Hűtött áru? (i/n): ");
        worksheet.updateCooled(readBoolean());
        System.out.print("Raklapok száma: ");
        worksheet.setNumberOfPallets(Integer.parseInt(br.readLine()));
        boolean futureDateOnly = true;
        System.out.println("Foglalás időpontja (Példa: " + printDate(LocalDateTime.now()) + "):");
        worksheet.setReservedDate(readDate(futureDateOnly));
        System.out.print("Idő kerekítve: " + printDate(worksheet.getReservedDate()));

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

    // TODO: Kifejtendő
    private static void doKiszallitas(ObjectOutputStream oos, ObjectInputStream ois) {
    }

    // TODO: Kifejtendő
    private static void doUjKiszallitas(ObjectOutputStream oos, ObjectInputStream ois) {
    }

    // TODO: Kifejtendő
    private static void doBeszallitas(ObjectOutputStream oos, ObjectInputStream ois) {
    }

    // Igen vagy Nem szkenner
    private static boolean readBoolean () throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String userinput = "";
        // Csak igen-t vagy nem-et fogadunk el
        while (!userinput.equalsIgnoreCase("i") | !userinput.equalsIgnoreCase("n")){
            userinput = br.readLine().trim();
            if (userinput.equalsIgnoreCase("i")) return true;
            if (userinput.equalsIgnoreCase("n")) return false;
            System.out.println("Hibás válasz, próbáld újra (i/n): ");
        }
        return Boolean.parseBoolean(null);
    }

    // Dátum szkenner
    private static LocalDateTime readDate(boolean futureDateOnly) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = null;

        // Csak jó dátumot fogadunk el
        while (dateTime == null) {
            String userinput = br.readLine();
            try {
                dateTime = LocalDateTime.parse(userinput, format);
            } catch (DateTimeParseException e) {
                System.out.println("Hibás dátum formátum, próbáld újra: ");
            }
            // Ha futureDateOnly flag igaz, csak jövőbeni dűtumot fogadunk el
            if (dateTime!= null && futureDateOnly && Boolean.TRUE.equals(dateTime.isBefore(LocalDateTime.now()))){
                System.out.println("Csak jövőbeni dátum lehet, próbáld újra: ");
                dateTime = null;
            }
        }

        // Vágás 30 perces intervallumra
        dateTime = dateTime.truncatedTo(ChronoUnit.HOURS).plusMinutes(30 * (dateTime.getMinute() / 30));
        return dateTime;
    }

    // Dátum printer
    private static String printDate(LocalDateTime localDateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = formatter.format(localDateTime);
        return formattedDateTime;
    }

    // Teszt metódus
    private static void doTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
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
        Test test = new Test(firstNumber,secondNumber);
        oos.writeObject(test);
        // Szerver válaszának a kiolvasása az InputStream-ről
        Test returnTest = (Test)ois.readObject();
        System.out.println("Eredmény: " + returnTest.getResult());
    }

    private static void doFoglalasTest(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {

        // Teszt foglalási adatok - TODO: további teszteket hozzáadni
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

        // Inicializálás - TODO ciklusba tenni
        for (int i = 0; i < testReservations.size(); i++) {
            Worksheet worksheet = new Worksheet(Worksheet.WorkType.Incoming);
            worksheet.setTransaction(Worksheet.TransactionType.Initialize);
            oos.writeObject(worksheet);
            worksheet = (Worksheet) ois.readObject();

            // Kitöltés
            Reservation reservation = testReservations.get(i);
            worksheet = fillTestWorkSheet(worksheet, reservation);

            // Jóváhagyás
            worksheet.setTransaction(Worksheet.TransactionType.Approve);
            oos.writeObject(worksheet);
            worksheet = (Worksheet) ois.readObject();

            // Tömör kiírás
            printTestWorksheet(worksheet);
        }
    }

    private static Worksheet fillTestWorkSheet(Worksheet worksheet, Reservation reservation) {
        worksheet.setRenterID(reservation.RenterID);
        worksheet.setExternalID(reservation.PartNumber);
        worksheet.updateCooled(reservation.IsCooled);
        worksheet.setNumberOfPallets(reservation.Pallets);
        worksheet.setReservedDate(reservation.ReservationDate);
        return worksheet;
    }

    private static void printTestWorksheet(Worksheet worksheet) {
        System.out.print("\nID: " + worksheet.getTransactionID() + " (" + worksheet.isInitialized() + ")");
        System.out.print("\tBérlő: " + worksheet.getRenterID());
        System.out.print("\tCikk: " + worksheet.getExternalID());
        System.out.print("\tHűtött: " + worksheet.isCooled());
        System.out.print("\tRaklap: " + worksheet.getNumberOfPallets());
        System.out.print("\tIdő: " + printDate(worksheet.getReservedDate()));
        System.out.print("\tFoglalás: ");
        System.out.print(worksheet.isApproved() ? "OK" : "NOK - " + worksheet.getTransactionMessage());
    }


}
