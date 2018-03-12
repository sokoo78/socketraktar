package com.berraktar;

import java.io.Serializable;

public class Employee implements Serializable {

    private static final long serialVersionUID = 2435010565803855201L;
    public enum UserType {Dispatcher, StoreKeeper}

    // Dolgozók közös tulajdonságai
    private String name;
    private UserType position;

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
}
