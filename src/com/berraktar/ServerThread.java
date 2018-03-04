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
        String userName = null;
        try {
            // Ki-, és bemeneti csatornák létrhozása
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // Új kliens bejelentkezés fogadása
            userName = (String) objectInputStream.readObject();
            System.out.println("Új kliens csatlakozás: " + userName);

            // Beérkező objektumok feldolgozása
            while ((object = objectInputStream.readObject()) != null){
                // Message típusú üzenet feldolgozása
                if (object instanceof Message ) {
                    doFoglalas((Message)object, userName);
                    // Feldolgozott objektum visszaküldése a kliensnek
                    objectOutputStream.writeObject(object);
                }
            }
            // TODO: További beérkező objektumok feldolgozása

            // Zárjuk a kapcsolatot
            socket.close();
        } catch (IOException e) {
            System.out.println("Kliens bontotta a kapcsolatot: " + userName);
        } catch (ClassNotFoundException e) {
            System.out.println("Kliens érvénytelen adatot küldött: " + userName);
        }
    }

    // TODO: Példa eljárást átírni a véglegesre
    private void doFoglalas(Message message, String userName) {
        // Összeszorozzuk a két számot amit kaptunk az objektumban
        message.setResult(message.getFirstNumber().intValue() * message.getSecondNumber().intValue());
        System.out.println("DoFoglalas eljárást hívta: " + userName);
    }
}
