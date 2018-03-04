package com.berraktar;

// Külön szál a klienseknek

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {

    Socket socket;

    ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {

        Message message;
        try {
            // Ki-, és bemeneti csatornák létrhozása
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // Új kliens bejelentkezés fogadása
            String userName = (String)objectInputStream.readObject();
            System.out.println("Új kliens csatlakozás: " + userName);

            // Objektumok fogadása a klienstől
            while ((message = (Message)objectInputStream.readObject()) != null) {
                // Fogadott objektum feldolgozása
                doSomething(message, userName);
                // Feldolgozott objektum visszaküldése a kliensnek
                objectOutputStream.writeObject(message);
            }
            // Zárjuk a kapcsolatot
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void doSomething(Message message, String userName) {
        // Összeszorozzuk a két számot amit kaptunk az objektumban
        message.setResult(message.getFirstNumber().intValue() * message.getSecondNumber().intValue());
        System.out.println("DoSomething procedure has been called by " + userName);
    }
}
