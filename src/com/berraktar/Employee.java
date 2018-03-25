package com.berraktar;

import java.io.Serializable;

public class Employee implements Serializable {

    // Ez a szerializációhoz kell
    private static final long serialVersionUID = 2435010565803855201L;

    // Dolgozó típusok
    public enum UserType {Dispatcher, Storekeeper, Reports, Tests}

    // Állapotjelzők
    private boolean isLoggedin = false;

    // Dolgozók közös tulajdonságai
    private String name;
    private UserType position;
    private String password;

    // Konstruktor
    Employee(String name, UserType position) {
        this.name = name;
        this.position = position;
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
