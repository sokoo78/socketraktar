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
        DoBejelentkezes(objectOutputStream);

        // Menü
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("\nBérraktár menü:\n\t1. Új Foglalás\n\t2. Beszállítás\n\t3. Új kiszállítás\n\t4. Kiszállítás\n\t5. Kilépés\nVálassz menüpontot: ");
        String input = br.readLine();
        while(!input.equals("5")){
            switch(input){
                case "1":
                    doFoglalas(objectOutputStream, objectInputStream);
                    break;
                case "2":
                    doBeszallitas(objectOutputStream, objectInputStream);
                    break;
                case "3":
                    doUjKiszallitas(objectOutputStream, objectInputStream);
                    break;
                case "4":
                    doKiszallitas(objectOutputStream, objectInputStream);
                    break;
                default:
                    System.out.println("A megadott menüpont nem létezik! (" + input + ")");
            }
            System.out.print("\nNyomj ENTER-t a folytatáshoz!");
            System.in.read();
            System.out.print("\nBérraktár menü:\n\t1. Új Foglalás\n\t2. Beszállítás\n\t3. Új kiszállítás\n\t4. Kiszállítás\n\t5. Kilépés\nVálassz menüpontot: ");
            input = br.readLine();
        }

        // Kapcsolat bontása
        socket.close();
    }

    private static void DoBejelentkezes(ObjectOutputStream oos) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Felhasználó:");
        String userName = br.readLine();
        System.out.print("Beosztás (1 - diszpécser, 2 - raktáros):");
        int userPosition = Integer.parseInt(br.readLine());
        // Név küldése a szervernek
        Employee employee = new Employee(userName, Employee.UserType.values()[userPosition]);
        oos.writeObject(employee);
    }

    // TODO: Példa eljárást átírni a véglegesre
    private static void doFoglalas(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Integer firstNumber = null;
        Integer secondNumber = null;
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
        oos.writeObject(message);
        // Szerver válaszának a kiolvasása az InputStream-ről
        Message returnMessage = (Message)ois.readObject();
        System.out.println("Eredmény: " + returnMessage.getResult());
    }

    // TODO: Kifejtendő
    private static void doKiszallitas(ObjectOutputStream oos, ObjectInputStream ois) {
    }

    // TODO: Kifejtendő
    private static void doUjKiszallitas(ObjectOutputStream oos, ObjectInputStream ois) {
    }

    // TODO: Kifejtendő
    private static void doBeszallitas(ObjectOutputStream oos, ObjectInputStream ois) {
    }
}
