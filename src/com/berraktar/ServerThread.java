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
                if (object instanceof ReservationMessage) {
                    doReservation(objectOutputStream, (ReservationMessage)object, employee.getName());
                }

                // ReceivingMessage típusú üzenet feldolgozása
                if (object instanceof ReceivingMessage) {
                    doReceiving(objectOutputStream, (ReceivingMessage)object, employee.getName());
                }

                // UnloadingMessage típusú üzenet feldolgozása
                if (object instanceof UnloadingMessage) {
                    doUnloading(objectOutputStream, (UnloadingMessage)object, employee.getName());
                }

                // ServerTest típusú üzenet feldolgozása
                if (object instanceof ServerTest) {
                    doServerTest(objectOutputStream, (ServerTest)object, employee.getName());
                }

                // ReportMessage típusú üzenet feldolgozása
                if (object instanceof ReportMessage) {
                    doReport(objectOutputStream, (ReportMessage)object, employee.getName());
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

    private void doReport(ObjectOutputStream oos, ReportMessage reportMessage, String userName) throws IOException {
        switch (reportMessage.getReportType()){
            case Renters:
                reportMessage = Reporting.RenterReport(reportMessage, accounting);
                oos.writeObject(reportMessage);
                System.out.println(userName + " jelentést kért: " + reportMessage.getReportType());
                break;
            case Worksheets:
                reportMessage = warehouse.WorksheetReport(reportMessage);
                oos.writeObject(reportMessage);
                System.out.println(userName + " jelentést kért: " + reportMessage.getReportType());
                break;
            case Locations:
                reportMessage = warehouse.LocationReport(reportMessage);
                oos.writeObject(reportMessage);
                System.out.println(userName + " jelentést kért: " + reportMessage.getReportType());
                break;
            case Terminals:
                reportMessage = warehouse.TerminalReport(reportMessage);
                oos.writeObject(reportMessage);
                System.out.println(userName + " jelentést kért: " + reportMessage.getReportType());
                break;
            case Receivings:
                break;
            case Shipments:
                break;
        }
    }

    // Új foglalás
    private void doReservation(ObjectOutputStream oos, ReservationMessage reservationMessage, String userName) throws IOException {
        // Munkalap létrehozása, ha még nincs létrehozva
        if (!reservationMessage.isCreated()) {
            reservationMessage.setTransactionID(warehouse.CreateWorkSheet(reservationMessage.getWorkSheetType()));
            reservationMessage.setCreated();
            oos.writeObject(reservationMessage);
            System.out.println(userName + " új munkalapot hozott létre - transactionID: " + reservationMessage.getTransactionID());
        }
        // Foglalás jóváhagyása
        else {
            reservationMessage = warehouse.ApproveWorkSheet(reservationMessage, accounting);
            oos.writeObject(reservationMessage);
            System.out.println(userName + " munkalapot küldött jóváhagyásra - transactionID: " + reservationMessage.getTransactionID());
        }
}

    // Beérkezés
    private void doReceiving(ObjectOutputStream oos, ReceivingMessage receivingMessage, String userName) throws IOException {
        // Munkalap aktiválása, ha még nem aktív
        if (!receivingMessage.isApproved()){
            receivingMessage = warehouse.ActivateWorkSheet(receivingMessage);
            oos.writeObject(receivingMessage);
            System.out.println(userName + " munkalapot küldött aktiválásra - transactionID: " + receivingMessage.getTransactionID());
        }
        // Beérkeztetés indítása
        else if (!receivingMessage.isUnloaded()){
            receivingMessage = warehouse.ProcessWorkSheet(receivingMessage);
            oos.writeObject(receivingMessage);
            System.out.println(userName + " munkalapot küldött végrehajtásra - transactionID: " + receivingMessage.getTransactionID());
        } else {
        // Beérkezés visszaigazolása, és lezárása
            receivingMessage = warehouse.CompleteWorkSheet(receivingMessage, accounting);
            oos.writeObject(receivingMessage);
            System.out.println(userName + " munkalapot küldött lezárásra - transactionID: " + receivingMessage.getTransactionID());
        }
    }

    private void doUnloading(ObjectOutputStream oos, UnloadingMessage unloadingMessage, String userName) throws IOException {
        unloadingMessage = warehouse.UnloadWorkSheet(unloadingMessage);
        oos.writeObject(unloadingMessage);
        System.out.println(userName + " munkalapot küldött kirakodásra - transactionID: " + unloadingMessage.getTransactionID());
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
