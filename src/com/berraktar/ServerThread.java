package com.berraktar;

// Külön szál a klienseknek

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

class ServerThread extends Thread {

    // Socket átvétele a főszáltól
    private Socket socket;
    private Warehouse warehouse;
    private Accounting accounting;

    ServerThread(Socket socket, Warehouse warehouse, Accounting accounting) {
        this.socket = socket;
        this.warehouse = warehouse;
        this.accounting = accounting;
    }

    // Új szál indítása
    public void run() {

        Object object;
        Employee employee = null;
        try {
            // Ki-, és bemeneti csatornák létrehozása
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // Új kliens bejelentkezés fogadása - TODO logint felokosítani, ha kell..
            employee = (Employee)objectInputStream.readObject();
            employee.setLoggedin();
            System.out.println("Új kliens csatlakozott: " + employee.getName() + ", " + employee.getPosition());
            objectOutputStream.writeObject(employee);

            // Beérkező objektumok feldolgozása
            while ((object = objectInputStream.readObject()) != null){

                // Worksheet típusú üzenet feldolgozása
                if (object instanceof Reservation) {
                    doReservation(objectOutputStream, (Reservation)object, employee.getName());
                }

                // Receiving típusú üzenet feldolgozása
                if (object instanceof Receiving) {
                    doReceiving(objectOutputStream, (Receiving)object, employee.getName());
                }

                // Unloading típusú üzenet feldolgozása
                if (object instanceof Unloading) {
                    doUnloading(objectOutputStream, (Unloading)object, employee.getName());
                }

                // ServerTest típusú üzenet feldolgozása
                if (object instanceof ServerTest) {
                    doServerTest(objectOutputStream, (ServerTest)object, employee.getName());
                }

                // Report típusú üzenet feldolgozása
                if (object instanceof Report) {
                    doReport(objectOutputStream, (Report)object, employee.getName());
                }
            }

            // Zárjuk a kapcsolatot
            socket.close();
        } catch (IOException e) {
            System.out.println(Objects.requireNonNull(employee).getName() + " bontotta a kapcsolatot ");
        } catch (ClassNotFoundException e) {
            System.out.println(Objects.requireNonNull(employee).getName() + " érvénytelen adatot küldött ");
        }
    }

    private void doReport(ObjectOutputStream oos, Report report, String userName) throws IOException {
        switch (report.getReportType()){
            case Renters:
                report = warehouse.RenterReport(report);
                oos.writeObject(report);
                System.out.println(userName + " jelentést kért: " + report.getReportType());
                break;
            case Worksheets:
                report = warehouse.WorksheetReport(report);
                oos.writeObject(report);
                System.out.println(userName + " jelentést kért: " + report.getReportType());
                break;
            case Locations:
                report = warehouse.LocationReport(report);
                oos.writeObject(report);
                System.out.println(userName + " jelentést kért: " + report.getReportType());
                break;
            case Terminals:
                report = warehouse.TerminalReport(report);
                oos.writeObject(report);
                System.out.println(userName + " jelentést kért: " + report.getReportType());
                break;
            case Receivings:
                break;
            case Shipments:
                break;
        }
    }

    // Új foglalás
    private void doReservation(ObjectOutputStream oos, Reservation reservation, String userName) throws IOException {
        // Munkalap létrehozása, ha még nincs létrehozva
        if (!reservation.isCreated()) {
            reservation.setTransactionID(warehouse.CreateWorkSheet(reservation.getWorkSheetType()));
            reservation.setCreated();
            oos.writeObject(reservation);
            System.out.println(userName + " új munkalapot hozott létre - transactionID: " + reservation.getTransactionID());
        }
        // Foglalás jóváhagyása
        else {
            reservation = warehouse.ApproveWorkSheet(reservation, accounting);
            oos.writeObject(reservation);
            System.out.println(userName + " munkalapot küldött jóváhagyásra - transactionID: " + reservation.getTransactionID());
        }
}

    // Beérkezés
    private void doReceiving(ObjectOutputStream oos, Receiving receiving, String userName) throws IOException {
        // Munkalap aktiválása, ha még nem aktív
        if (!receiving.isApproved()){
            receiving = warehouse.ActivateWorkSheet(receiving);
            oos.writeObject(receiving);
            System.out.println(userName + " munkalapot küldött aktiválásra - transactionID: " + receiving.getTransactionID());
        }
        // Beérkeztetés indítása
        else if (!receiving.isUnloaded()){
            receiving = warehouse.ProcessWorkSheet(receiving);
            oos.writeObject(receiving);
            System.out.println(userName + " munkalapot küldött végrehajtásra - transactionID: " + receiving.getTransactionID());
        } else {
        // Beérkezés visszaigazolása, és lezárása
            receiving = warehouse.CompleteWorkSheet(receiving, accounting);
            oos.writeObject(receiving);
            System.out.println(userName + " munkalapot küldött lezárásra - transactionID: " + receiving.getTransactionID());
        }
    }

    private void doUnloading(ObjectOutputStream oos, Unloading unloading, String userName) throws IOException {
        unloading = warehouse.UnloadWorkSheet(unloading);
        oos.writeObject(unloading);
        System.out.println(userName + " munkalapot küldött kirakodásra - transactionID: " + unloading.getTransactionID());
    }

    // Szerver teszt
    private void doServerTest(ObjectOutputStream oos, ServerTest serverTest, String userName) throws IOException {
        // Összeszorozzuk a két számot amit kaptunk az objektumban
        serverTest.setResult(serverTest.getFirstNumber() * serverTest.getSecondNumber());
        System.out.println("DoTest metódust hívta: " + userName);
        // Feldolgozott objektum visszaküldése a kliensnek
        oos.writeObject(serverTest);
    }
}
