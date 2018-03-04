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

        // Adatok bekérése a konzolról
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Bejelentkezési név bekérése
        System.out.print("Felhasználó:");
        String userName = br.readLine();
        // Név küldése a szervernek
        objectOutputStream.writeObject(userName);

        // Adatok bekérése a message objektum számára
        Integer firstNumber = null;
        Integer secondNumber = null;
        while (true){
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
            objectOutputStream.writeObject(message);
            // Szerver válaszának a kiolvasása az InputStream-ről
            Message returnMessage = (Message)objectInputStream.readObject();
            System.out.println("Eredmény: " + returnMessage.getResult());
        }
        // Kapcsolat bontása
        //socket.close();
    }
}
