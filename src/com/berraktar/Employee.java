package com.berraktar;

import java.io.Serializable;

public class Employee implements Serializable {

    // Ez a szerializációhoz kell
    private static final long serialVersionUID = 2435010565803855201L;

    // Dolgozó típusok
    public enum UserType {Dispatcher, Storekeeper, Reports, Tests}

    // Tranzakció flagek
    public enum TransactionType {Login, Logout}
    // Login: Bejelentkezési adatok ellenőrzése - szerver isLoggedin flaggel tér vissza
    // Logout: Kijelentkezés

    // Állapotjelzők
    private boolean isLoggedin = false;

    // Dolgozók közös tulajdonságai
    private String name;
    private UserType position;
    private String password;

    // Konstruktor (super)
    public Employee(String name, UserType position) {
        this.name = name;
        this.position = position;
    }

    // Dolgozók közös metódusai

    protected void showMenu(){
        // TODO ehhez javítani kell az ojjektum létrehozást..
    }

    // Getterek, setterek

    public String getName() {
        if (this.name != null) return this.name;
        else return "n/a";
    }

    public int getPositionID(){
        return this.position.ordinal()+1;
    }

    public UserType getPosition() {
        return position;
    }

    public boolean isLoggedin() {
        return isLoggedin;
    }

    public void setLoggedin() {
        isLoggedin = true;
    }
}
