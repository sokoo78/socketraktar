package com.berraktar;

// Socket kliens

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

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
            System.out.print("\nBérraktár menü:\n\t" +
                    "1. Új Foglalás\n\t" +
                    "2. Beszállítás\n\t" +
                    "3. Új kiszállítás\n\t" +
                    "4. Kiszállítás\n\t" +
                    "5. Teszt\n\t" +
                    "6. Kilépés\n" +
                    "Válassz menüpontot: ");
            String input = br.readLine();
            while(!input.equals("6")){
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
                        doTest(oos, ois);
                        break;
                    default:
                        System.out.println("A megadott menüpont nem létezik! (" + input + ")");
                }
                System.out.print("\nNyomj ENTER-t a folytatáshoz!");
                System.in.read();
                System.out.print("\nBérraktár menü:\n\t" +
                        "1. Új Foglalás\n\t" +
                        "2. Beszállítás\n\t" +
                        "3. Új kiszállítás\n\t" +
                        "4. Kiszállítás\n\t" +
                        "5. Teszt\n\t" +
                        "6. Kilépés\n" +
                        "Válassz menüpontot: ");
                input = br.readLine();
            }
        }
        // TODO: Raktáros menü
        // TODO: Könyvelés menü (lekérdezések)
    }

    // TODO: Buta login, ellenőrzést és visszatérést betenni
    private static Employee DoBejelentkezes(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Felhasználó:");
        String userName = br.readLine();
        System.out.print("Beosztás (1 - diszpécser, 2 - raktáros):");
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

    // TODO: Nincs kész
    private static void doFoglalas(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Tranzakciószám kérése a szervertől
        Worksheet worksheet = new Worksheet(Worksheet.WorkType.Incoming);
        worksheet.setTransaction(Worksheet.TransactionType.Initialize);
        oos.writeObject(worksheet);
        worksheet = (Worksheet)ois.readObject();

        // Kérelem adatainak bekérése a munkalapra
        System.out.print("\nÚj munkalap létrehozva - Tranzakcióazonosító: " + worksheet.getTransactionID() + " (isInitialized: " + worksheet.isInitialized() + ")");
        System.out.print("\nVevőkód:");
        worksheet.setRenterID(br.readLine());
        System.out.print("Cikkszám:");
        worksheet.setExternalID(br.readLine());
        System.out.print("Raklapok száma:");
        worksheet.setNumberOfPallets(Integer.parseInt(br.readLine()));
        worksheet.setReservedDate(readDate());
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
            System.out.print("\nSzerver üzenete:" + worksheet.getTransactionMessage());
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

    // Dátum szkenner
    private static LocalDateTime readDate() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        System.out.println("Foglalás időpontja (Példa: " + printDate(LocalDateTime.now()) + "):");
        LocalDateTime dateTime = null;
        while (dateTime == null) {
            String userinput = br.readLine();
            try {
                dateTime = LocalDateTime.parse(userinput, format);
            } catch (DateTimeParseException e) {
                System.out.println("Hibás dátum formátum, próbáld újra!");
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
        Message message = new Message(firstNumber,secondNumber);
        oos.writeObject(message);
        // Szerver válaszának a kiolvasása az InputStream-ről
        Message returnMessage = (Message)ois.readObject();
        System.out.println("Eredmény: " + returnMessage.getResult());
    }
}
