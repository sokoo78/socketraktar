// Külön szerver szál a klienseknek
package com.berraktar;

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
                if (object instanceof MessageCreate)     callCreate(objectOutputStream, (MessageCreate) object, employee.getName());
                if (object instanceof MessageReserve)    callReserve(objectOutputStream, (MessageReserve) object, employee.getName());
                if (object instanceof MessageActivate)   callActivate(objectOutputStream, (MessageActivate) object, employee.getName());
                if (object instanceof MessageProcess)    callProcess(objectOutputStream, (MessageProcess) object, employee.getName());
                if (object instanceof MessageUnload)     callUnload(objectOutputStream, (MessageUnload) object, employee.getName());
                if (object instanceof MessageLoad)       callLoad(objectOutputStream, (MessageLoad) object, employee.getName());
                if (object instanceof MessageStore)      callStore(objectOutputStream, (MessageStore) object, employee.getName());
                if (object instanceof MessageOrder)      callOrder(objectOutputStream, (MessageOrder) object, employee.getName());
                if (object instanceof MessageShip)       callShip(objectOutputStream, (MessageShip) object, employee.getName());
                if (object instanceof MessageServerTest) callServerTest(objectOutputStream, (MessageServerTest) object, employee.getName());
                if (object instanceof MessageReport)     callReport(objectOutputStream, (MessageReport) object, employee.getName());
            }

            // Zárjuk a kapcsolatot
            socket.close();
        } catch (IOException e) {
            System.out.println(Objects.requireNonNull(employee).getName() + " bontotta a kapcsolatot ");
        } catch (ClassNotFoundException e) {
            System.out.println(Objects.requireNonNull(employee).getName() + " érvénytelen adatot küldött ");
        }
    }

    // Új munkalap
    private void callCreate(ObjectOutputStream oos, MessageCreate messageCreate, String userName) throws IOException {
        messageCreate.setTransactionID(WorkOrders.CreateWorkSheet(messageCreate));
        oos.writeObject(messageCreate);
        System.out.println(userName + " új munkalapot hozott létre - transactionID: " + messageCreate.getTransactionID());
    }

    // Új foglalás
    private void callReserve(ObjectOutputStream oos, MessageReserve messageReserve, String userName) throws IOException {
        messageReserve = warehouse.DoReservation(messageReserve, accounting);
        oos.writeObject(messageReserve);
        System.out.println(userName + " munkalapot küldött jóváhagyásra - transactionID: " + messageReserve.getTransactionID());
    }

    // Új kiszállítási igény
    private void callOrder(ObjectOutputStream oos, MessageOrder messageOrder, String userName) throws IOException {
        messageOrder = warehouse.DoOrder(messageOrder, accounting);
        oos.writeObject(messageOrder);
        System.out.println(userName + " munkalapot küldött jóváhagyásra - transactionID: " + messageOrder.getTransactionID());
    }

    // Munkalap aktiválása
    private void callActivate(ObjectOutputStream oos, MessageActivate messageActivate, String userName) throws IOException {
        messageActivate = WorkOrders.ActivateWorkSheet(messageActivate);
        oos.writeObject(messageActivate);
        System.out.println(userName + " munkalapot küldött aktiválásra - transactionID: " + messageActivate.getTransactionID());
    }

    // Munkalap feldolgozásának indítása
    private void callProcess(ObjectOutputStream oos, MessageProcess messageProcess, String userName) throws IOException {
        messageProcess = WorkOrders.ProcessWorkSheet(messageProcess);
        oos.writeObject(messageProcess);
        System.out.println(userName + " munkalapot küldött végrehajtásra - transactionID: " + messageProcess.getTransactionID());
    }

    // Kirakodás kocsiból terminálra
    private void callUnload(ObjectOutputStream oos, MessageUnload messageUnload, String userName) throws IOException {
        messageUnload = warehouse.DoUnloading(messageUnload);
        oos.writeObject(messageUnload);
        System.out.println(userName + " munkalapot küldött kirakodásra - transactionID: " + messageUnload.getTransactionID());
    }

    // Kirakodás raktárból terminálra
    private void callLoad(ObjectOutputStream oos, MessageLoad messageLoad, String userName) throws IOException {
        messageLoad = warehouse.DoLoading(messageLoad, accounting);
        oos.writeObject(messageLoad);
        System.out.println(userName + " munkalapot küldött rakodásra - transactionID: " + messageLoad.getTransactionID());
    }

    // Berakodás a raktárba
    private void callStore(ObjectOutputStream oos, MessageStore messageStore, String userName) throws IOException {
        messageStore = warehouse.DoStoring(messageStore, accounting);
        oos.writeObject(messageStore);
        System.out.println(userName + " munkalapot küldött lezárásra - transactionID: " + messageStore.getTransactionID());
    }

    // Kirakodás a kocsiba
    private void callShip(ObjectOutputStream oos, MessageShip messageShip, String userName) throws IOException {
        messageShip = warehouse.DoShipping(messageShip, accounting);
        oos.writeObject(messageShip);
        System.out.println(userName + " munkalapot küldött lezárásra - transactionID: " + messageShip.getTransactionID());
    }

    // Jelentések
    private void callReport(ObjectOutputStream oos, MessageReport messageReport, String userName) throws IOException {
        switch (messageReport.getReportType()){
            case Renters:
                messageReport = Reporting.RenterReport(messageReport, accounting.getRenters());
                oos.writeObject(messageReport);
                System.out.println(userName + " jelentést kért: " + messageReport.getReportType());
                break;
            case Worksheets:
                messageReport = Reporting.WorksheetReport(messageReport, WorkOrders.getWorksheets());
                oos.writeObject(messageReport);
                System.out.println(userName + " jelentést kért: " + messageReport.getReportType());
                break;
            case Locations:
                messageReport = Reporting.LocationReport(messageReport, warehouse);
                oos.writeObject(messageReport);
                System.out.println(userName + " jelentést kért: " + messageReport.getReportType());
                break;
            case Terminals:
                messageReport = Reporting.TerminalReport(messageReport, warehouse);
                oos.writeObject(messageReport);
                System.out.println(userName + " jelentést kért: " + messageReport.getReportType());
                break;
            case Receivings:
                break;
            case Shipments:
                break;
        }
    }

    // Szerver teszt
    private void callServerTest(ObjectOutputStream oos, MessageServerTest messageServerTest, String userName) throws IOException {
        // Összeszorozzuk a két számot amit kaptunk az objektumban
        messageServerTest.setResult(messageServerTest.getFirstNumber() * messageServerTest.getSecondNumber());
        System.out.println("DoTest metódust hívta: " + userName);
        // Feldolgozott objektum visszaküldése a kliensnek
        oos.writeObject(messageServerTest);
    }
}
