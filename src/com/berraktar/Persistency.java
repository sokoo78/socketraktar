package com.berraktar;

import java.io.*;

public final class Persistency {

    public static Object SaveObject(Object object, String fileName){
        try {
            FileOutputStream f = new FileOutputStream(new File(fileName));
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(object);
            o.close();
            f.close();
            return object;

        } catch (FileNotFoundException e) {
            System.out.println("Fájl nem található. (" + e.getMessage() + ")");
        } catch (IOException e) {
            System.out.println("Objektum stream inicializálása sikertelen. (" + e.getMessage() + ")");
        }
        return null;
    }

    public static Object LoadObject(String fileName){
        try {
            FileInputStream fi = new FileInputStream(new File(fileName));
            ObjectInputStream oi = new ObjectInputStream(fi);
            Object object = oi.readObject();
            oi.close();
            fi.close();
            return object;
        } catch (FileNotFoundException e) {
            System.out.println("Fájl nem található: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Objektum stream inicializálása sikertelen: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Érvénytelen osztály: " + e.getMessage());
        }
        return null;
    }
}