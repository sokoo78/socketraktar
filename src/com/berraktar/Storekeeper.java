package com.berraktar;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Storekeeper extends Employee {

    public Storekeeper(String name, UserType position) {
        super(name, position);
    }

    // TODO: Bevételzés
    public static void doReceiving(ObjectOutputStream oos, ObjectInputStream ois) {

    }
    // TODO: Kiszállítás
    public static void doShipping(ObjectOutputStream oos, ObjectInputStream ois) {

    }
}
