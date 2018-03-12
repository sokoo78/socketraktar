package com.berraktar;

// Külön szál a klienseknek

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {

    // Socket átvétele a főszáltól
    Socket socket;

    ServerThread(Socket socket) {
        this.socket = socket;
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
                // Message típusú üzenet feldolgozása
                if (object instanceof Message) {
                    doFoglalas((Message)object, employee.getName());
                    // Feldolgozott objektum visszaküldése a kliensnek
                    objectOutputStream.writeObject(object);
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

    // TODO: Példa eljárást átírni a véglegesre
    private void doFoglalas(Message message, String userName) {
        // Összeszorozzuk a két számot amit kaptunk az objektumban
        message.setResult(message.getFirstNumber().intValue() * message.getSecondNumber().intValue());
        System.out.println("DoFoglalas eljárást hívta: " + userName);
    }
}
