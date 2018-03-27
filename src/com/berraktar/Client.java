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
        Employee employee = doLogin(objectInputStream, objectOutputStream);

        // Menü megjelenítése
        showMenu(employee, objectOutputStream, objectInputStream);

        // Kapcsolat bontása
        socket.close();
    }

    // Menü megjelenítése
    private static void showMenu(Employee employee, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int positionID = employee.getPositionID();
        // Diszpécser menü
        if (positionID == 1){
            Dispatcher.ShowMenu(oos, ois);
        }
        // Raktáros menü
        if (positionID == 2){
            Storekeeper.ShowMenu(oos, ois);
        }
        // Jelentések menü
        if (positionID == 3){
            Reporting.ShowMenu(oos, ois);
        }
        // Tesztek menü
        if (positionID == 4){
            SystemTests.ShowMenu(oos, ois);
        }
    }

    // TODO: Buta login, ellenőrzést és visszatérést betenni
    private static Employee doLogin(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Felhasználói név:");
        String userName = br.readLine();
        System.out.print("Menü: (1 - diszpécser, 2 - raktáros, 3 - jelentések, 4 - tesztek):");
        int userPosition = Integer.parseInt(br.readLine())-1;
        // Név és pozíció küldése a szervernek
        Employee employee = new Employee(userName, Employee.UserType.values()[userPosition]);
        oos.writeObject(employee);
        // Bejelentkezés ellenőrzése
        employee = (Employee) ois.readObject();
        if (employee.isLoggedin()){
            System.out.print("Sikeres bejelentkezés: " + employee.getName() + ", " + employee.getPosition());
        } else {
            System.out.print("Sikertelen bejelentkezés!");
        }
        return employee;
    }

}
