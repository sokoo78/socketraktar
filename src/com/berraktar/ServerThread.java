package com.berraktar;

// Külön szál a klienseknek

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {

    // Socket átvétele a főszáltól
    Socket socket;
    Warehouse warehouse;
    Accounting accounting;

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
                if (object instanceof Worksheet) {
                    doWork(objectOutputStream, (Worksheet)object, employee.getName());
                }

                // Message típusú üzenet feldolgozása
                if (object instanceof Message) {
                    doTest(objectOutputStream, (Message)object, employee.getName());
                }

                // Report típusú üzenet feldolgozása
                if (object instanceof Report) {
                    doReport(objectOutputStream, (Report)object, employee.getName());
                }
            }
            // TODO: További beérkező objektumok feldolgozása

            // Zárjuk a kapcsolatot
            socket.close();
        } catch (IOException e) {
            System.out.println(employee.getName() + " bontotta a kapcsolatot ");
        } catch (ClassNotFoundException e) {
            System.out.println(employee.getName() + " érvénytelen adatot küldött ");
        }
    }

    private void doReport(ObjectOutputStream oos, Report report, String userName) throws IOException {
        switch (report.getReport()){
            case Renters:
                report = warehouse.RenterReport(report);
                oos.writeObject(report);
                System.out.println(userName + " jelentést kért: "); // + report.getReport());
                break;
            case Worksheets:
                report = warehouse.WorksheetReport(report);
                oos.writeObject(report);
                System.out.println(userName + " jelentést kért: "); // + report.getReport());
                break;
            case Locations:
                report = warehouse.LocationReport(report);
                oos.writeObject(report);
                System.out.println(userName + " jelentést kért: "); // + report.getReport());
                break;
        }
    }

    // Munkalapokat feldolgozó metódus
    private void doWork(ObjectOutputStream oos, Worksheet worksheet, String userName) throws IOException {
        switch(worksheet.getTransaction()) {
            // Új munkalap létrehozása
            case Initialize:
                worksheet = warehouse.CreateWorkSheet(worksheet.getWorkType());
                oos.writeObject(worksheet);
                System.out.println(userName + " új munkalapot hozott létre - TransactionID: " + worksheet.getTransactionID());
                break;
            case Approve:
                worksheet = warehouse.ApproveWorkSheet(worksheet, accounting);
                oos.writeObject(worksheet);
                System.out.println(userName + " munkalapot küldött jóváhagyásra - TransactionID: " + worksheet.getTransactionID());
                break;
            case Activate:
                worksheet = warehouse.ActivateWorkSheet(worksheet);
                oos.writeObject(worksheet);
                System.out.println(userName + " munkalapot küldött aktiválásra - TransactionID: " + worksheet.getTransactionID());
                break;
            case Confirm:
                worksheet = warehouse.ConfirmWorkSheet(worksheet);
                oos.writeObject(worksheet);
                System.out.println(userName + " munkalapot küldött befejezésre - TransactionID: " + worksheet.getTransactionID());
                break;
            case Cancel:
                worksheet = warehouse.CancelWorkSheet(worksheet);
                oos.writeObject(worksheet);
                System.out.println(userName + " munkalapot küldött lemondásra - TransactionID: " + worksheet.getTransactionID());
                break;
            default:
                System.out.println(userName + " a DoWork metódust hibás tranzakcióazonosítóval hívta - " + worksheet.getTransaction());
        }
    }

    // Teszt metódus
    private void doTest(ObjectOutputStream oos, Message message, String userName) throws IOException {
        // Összeszorozzuk a két számot amit kaptunk az objektumban
        message.setResult(message.getFirstNumber().intValue() * message.getSecondNumber().intValue());
        System.out.println("DoTest metódust hívta: " + userName);
        // Feldolgozott objektum visszaküldése a kliensnek
        oos.writeObject(message);
    }
}
