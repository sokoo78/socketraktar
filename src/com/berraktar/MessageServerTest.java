package com.berraktar;

import java.io.Serializable;

class MessageServerTest implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = 8745960972757367043L;
    // Ezt a két számot fogjuk megadni
    private Integer firstNumber;
    private Integer secondNumber;
    // Ebbe pedig várjuk vissza az eredményt
    // Össze fogjuk szorozni őket a szerveren
    private Integer result = null;

    MessageServerTest(Integer firstNumber, Integer secondNumber){
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
    }

    Integer getFirstNumber(){
        return firstNumber;
    }

    Integer getSecondNumber(){
        return secondNumber;
    }

    Integer getResult(){
        return result;
    }

    void setResult(Integer result){
        this.result = result;
    }
}
