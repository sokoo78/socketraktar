package com.berraktar;

import java.io.Serializable;

public class Employee implements Serializable {

    // Ez a szerializációhoz kell
    private static final long serialVersionUID = 2435010565803855201L;

    // Dolgozó típusok
    public enum UserType {Dispatcher, StoreKeeper}
    // Dispatcher:  diszpécser
    // StoreKeeper: raktáros

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserType getPosition() {
        return position;
    }

    public void setPosition(UserType position) {
        this.position = position;
    }


    public boolean isLoggedin() {
        return isLoggedin;
    }

    public void setLoggedin(boolean loggedin) {
        isLoggedin = loggedin;
    }
}
