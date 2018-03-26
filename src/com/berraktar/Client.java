package com.berraktar;

// Socket kliens

import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Socket kliens indítása és kapcsolódás a szerverhez
        Socket socket = new Socket("localhost", Server.PORT);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

        // Bejelentkezés
        Employee employee = DoBejelentkezes(objectInputStream, objectOutputStream);

        // Menü megjelenítése
        showMenu(employee, objectOutputStream, objectInputStream);

        // Kapcsolat bontása
        socket.close();
    }

    private static void showMenu(Employee employee, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int positionID = employee.getPositionID();
        // Dispatcher menü
        if (positionID == 1){
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("\nBérraktár diszpécser menü:\n\t" +
                    "1. Új Foglalás\n\t" +
                    "2. Beszállítás\n\t" +
                    "3. Új kiszállítás\n\t" +
                    "4. Kiszállítás\n\t" +
                    "5. Kilépés\n" +
                    "Válassz menüpontot: ");
            String input = br.readLine();
            while(!input.equals("5")){
                switch(input){
                    case "1":
                        Dispatcher.newReservation(oos, ois);
                        break;
                    case "2":
                        Dispatcher.startReceiving(oos, ois);
                        break;
                    case "3":
                        Dispatcher.newDelivery(oos, ois);
                        break;
                    case "4":
                        Dispatcher.startDelivery(oos, ois);
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
                        "5. Kilépés\n" +
                        "Válassz menüpontot: ");
                input = br.readLine();
            }
        }
        // Raktáros menü
        if (positionID == 2){
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("\nBérraktár raktáros menü:\n\t" +
                    "1. Beérkezések\n\t" +
                    "2. Kiszállítások\n\t" +
                    "3. Kilépés\n" +
                    "Válassz menüpontot: ");
            String input = br.readLine();
            while(!input.equals("3")){
                switch(input){
                    case "1":
                        Storekeeper.doReceiving(oos, ois);
                        break;
                    case "2":
                        Storekeeper.doShipping(oos, ois);
                        break;
                    default:
                        System.out.println("A megadott menüpont nem létezik! (" + input + ")");
                }
                System.out.print("\nNyomj ENTER-t a folytatáshoz!");
                System.in.read();
                System.out.print("\nBérraktár raktáros menü:\n\t" +
                        "1. Beérkezések\n\t" +
                        "2. Kiszállítások\n\t" +
                        "3. Kilépés\n" +
                        "Válassz menüpontot: ");
                input = br.readLine();
            }
        }
        // Reports menü
        if (positionID == 3){
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("\nBérraktár jelentések menü:\n\t" +
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
                        GetReport(oos, ois, MessageReport.ReportType.Renters);
                        break;
                    case "2":
                        GetReport(oos, ois, MessageReport.ReportType.Worksheets);
                        break;
                    case "3":
                        GetReport(oos, ois, MessageReport.ReportType.Locations);
                        break;
                    case "4":
                        GetReport(oos, ois, MessageReport.ReportType.Terminals);
                        break;
                    default:
                        System.out.println("A megadott menüpont nem létezik! (" + input + ")");
                }
                System.out.print("\nNyomj ENTER-t a folytatáshoz!");
                System.in.read();
                System.out.print("\nBérraktár jelentések menü:\n\t" +
                        "1. Ügyfél lista\n\t" +
                        "2. Munkalap lista\n\t" +
                        "3. Lokáció lista\n\t" +
                        "4. Terminál foglalási lista\n\t" +
                        "5. Kilépés\n" +
                        "Válassz menüpontot: ");
                input = br.readLine();
            }
        }

        // Tests menü
        if (positionID == 4){
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("\nBérraktár tesztek menü:\n\t" +
                    "1. Foglalás teszt\n\t" +
                    "2. Beérkezés teszt\n\t" +
                    "3. Bevételezés teszt\n\t" +
                    "4. Rendelés teszt\n\t" +
                    "5. Szerver teszt\n\t" +
                    "6. Kilépés\n" +
                    "Válassz menüpontot: ");
            String input = br.readLine();
            while(!input.equals("6")){
                switch(input){
                    case "1":
                        SystemTests.doReservationTest(oos, ois);
                        break;
                    case "2":
                        SystemTests.doActivationTest(oos, ois);
                        break;
                    case "3":
                        SystemTests.doProcessingTest(oos, ois);
                        break;
                    case "4":
                        SystemTests.doOrderingTest(oos, ois);
                        break;
                    case "5":
                        SystemTests.doServerTest(oos, ois);
                        break;
                    default:
                        System.out.println("A megadott menüpont nem létezik! (" + input + ")");
                }
                System.out.print("\nNyomj ENTER-t a folytatáshoz!");
                System.in.read();
                System.out.print("\nBérraktár tesztek menü:\n\t" +
                        "1. Foglalás teszt\n\t" +
                        "2. Beérkezés teszt\n\t" +
                        "3. Bevételezés teszt\n\t" +
                        "4. Szerver teszt\n\t" +
                        "5. Kilépés\n" +
                        "Válassz menüpontot: ");
                input = br.readLine();
            }
        }
    }

    // Jelentések
    private static void GetReport(ObjectOutputStream oos, ObjectInputStream ois, MessageReport.ReportType reportType) throws IOException, ClassNotFoundException {
        MessageReport messageReport = new MessageReport(reportType);
        oos.writeObject(messageReport);
        messageReport = (MessageReport) ois.readObject();
        System.out.println(messageReport.getReply());
    }

    // TODO: Buta login, ellenőrzést és visszatérést betenni
    private static Employee DoBejelentkezes(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Felhasználói név:");
        String userName = br.readLine();
        System.out.print("Menü: (1 - diszpécser, 2 - raktáros, 3 - jelentések, 4 - tesztek):");
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

}
