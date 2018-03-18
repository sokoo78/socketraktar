package com.berraktar;

import java.io.*;

public class Storekeeper extends Employee {

    public Storekeeper(String name, UserType position) {
        super(name, position);
    }

    // TODO: Bevételzés
    public static void doReceiving(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Tranzakció azonosító: ");         // Munkalap sorszáma
        int transactionID = Integer.parseInt(br.readLine());
        // TODO: Incoming tranzakciót létrehozni
        // TODO: Lekérni a foglalási adatokat (Bérlő, terminál, raklapok, lokációk)
        // TODO: ciklus < ahány paletta van:
        // TODO:    - bekérni a bérlő cikkszámát (externalPartNumber)
        // TODO:    - bekérni a belső cikkszámot (internalPartNumber) -> ehhez kell egy generátor eljárás a szerveren
        // TODO:    - kiírni  a cikkszámokat
        // TODO: jóváhagyás kérése -> lokációba helyezés
        // TODO: felszabadítani a terminált, lejelenteni a logisztikai műveletet
        // TODO: a log műveleteket átrakni a munkalapra, és csak annak megszűnésekor lejelenteni (confirmed / cancel esetén)

    }
    // TODO: Kiszállítás
    public static void doShipping(ObjectOutputStream oos, ObjectInputStream ois) {

    }
}
