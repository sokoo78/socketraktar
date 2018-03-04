package com.berraktar;

// Példa Serializable objektumra

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 8745960972757367043L;
    // Ezt a két számot fogjuk megadni
    Integer firstNumber = null;
    Integer secondNumber = null;
    // Ebbe pedig várjuk vissza az eredményt
    // Össze fogjuk szorozni őket a szerveren
    Integer result = null;

    public Message(Integer firstNumber, Integer secondNumber){
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
    }

    public Integer getFirstNumber(){
        return firstNumber;
    }

    public Integer getSecondNumber(){
        return secondNumber;
    }

    public Integer getResult(){
        return result;
    }

    public void setResult(Integer result){
        this.result = result;
    }
}
