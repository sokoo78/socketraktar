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

    ServerThread(Socket socket, Warehouse warehouse) {
        this.socket = socket;
        this.warehouse = warehouse;
    }

    // Új szál indítása
    public void run() {

        Object object;
        Employee employee = null;
        try {
            // Ki-, és bemeneti csatornák létrehozása
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // Új kliens bejelentkezés fogadása
            employee = (Employee)objectInputStream.readObject();
            System.out.println("Új kliens csatlakozott: " + employee.getName() + ", " + employee.getPosition());

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
            }
            // TODO: További beérkező objektumok feldolgozása

            // Zárjuk a kapcsolatot
            socket.close();
        } catch (IOException e) {
            System.out.println("Kliens bontotta a kapcsolatot: " + employee.getName());
        } catch (ClassNotFoundException e) {
            System.out.println("Kliens érvénytelen adatot küldött: " + employee.getName());
        }
    }

    // Munkalapokat feldolgozó metódus
    private void doWork(ObjectOutputStream oos, Worksheet worksheet, String userName) throws IOException {
        switch(worksheet.getTransaction()) {
            // Új munkalap létrehozása
            case Initialize:
                worksheet = warehouse.CreateWorkSheet(worksheet.getWorkType());
                oos.writeObject(worksheet);
                System.out.println("DoWork/Initialize metódust hívta: " + userName);
                break;
            case Approve:
                break;
            case Activate:
                break;
            case Confirm:
                break;
            case Cancel:
                break;
            default:
                System.out.println("DoWork metódust hibás tranzakcióazonosítóval hívta: " + userName);
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
