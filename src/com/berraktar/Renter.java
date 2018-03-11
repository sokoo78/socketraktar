package com.berraktar;

import java.io.Serializable;

public class Renter implements Serializable {
    private static final long serialVersionUID = 3681656355503305496L;
    private String name;
    private String code;
    private int rentedNormalLocations;
    private int rentedCooledLocations;
    private int freeNormalLocations;
    private int freeCooledLocations;
    private int numberOfLogisticsOperations;

    Renter(String name, String code, int rentedNormalLocations, int rentedCooledLocations, int freeNormalLocations, int freeCooledLocations, int numberOfLogisticsOperations) {
        this.name = name;
        this.code = code;
        this.rentedNormalLocations = rentedNormalLocations;
        this.rentedCooledLocations = rentedCooledLocations;
        this.freeNormalLocations = freeNormalLocations;
        this.freeCooledLocations = freeCooledLocations;
        this.numberOfLogisticsOperations = numberOfLogisticsOperations;
    }

    // Logisztikai műveletek hozzáadása
    public void addLogisticsOperations(int numberOfOperations){
        this.numberOfLogisticsOperations += numberOfOperations;
    }

    // Szabad normál lokációk csökkentése
    public boolean decreaseFreeNormalSpace(int byNumberOfSpaces){
        if (byNumberOfSpaces <= this.freeNormalLocations){
            this.freeNormalLocations -= byNumberOfSpaces;
            return true;
        }
        return false;
    }

    // Szabad normál lokációk növelése
    public boolean increaseFreeNormalSpace(int byNumberOfSpaces){
        if ((this.freeNormalLocations + byNumberOfSpaces) <= this.rentedNormalLocations){
            this.freeNormalLocations += byNumberOfSpaces;
            return true;
        }
        return false;
    }

    // Szabad hűtött lokációk csökkentése
    public boolean decreaseFreeCooledSpace(int byNumberOfSpaces){
        if (byNumberOfSpaces <= this.freeCooledLocations){
            this.freeCooledLocations -= byNumberOfSpaces;
            return true;
        }
        return false;
    }

    // Szabad hűtött lokációk növelése
    public boolean increaseFreeCooledSpace(int byNumberOfSpaces){
        if ((this.freeCooledLocations + byNumberOfSpaces) <= this.rentedCooledLocations){
            this.freeCooledLocations += byNumberOfSpaces;
            return true;
        }
        return false;
    }
}
