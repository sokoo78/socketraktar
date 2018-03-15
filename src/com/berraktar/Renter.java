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

    public int getFreeLocations(boolean isCooled){
        if (isCooled){
            return this.freeCooledLocations;
        }
        return this.freeNormalLocations;
    }

    // Logisztikai műveletek hozzáadása
    public void addLogisticsOperations(int numberOfOperations){
        this.numberOfLogisticsOperations += numberOfOperations;
    }

    // Szabad normál lokációk növelése
    public boolean increaseFreeSpace(boolean isCooled, int byNumberOfSpaces){
        if (isCooled & ((freeCooledLocations + byNumberOfSpaces) <= this.rentedCooledLocations)){
            this.freeCooledLocations += byNumberOfSpaces;
            return true;
        }
        if (!isCooled & ((rentedNormalLocations + byNumberOfSpaces) <= this.rentedNormalLocations)){
            this.freeNormalLocations += byNumberOfSpaces;
            return true;
        }
        return false;
    }

    // Szabad normál lokációk csökkentése
    public boolean decreaseFreeSpace(boolean isCooled, int byNumberOfSpaces){
        if (isCooled & (byNumberOfSpaces <= this.freeCooledLocations)){
            this.freeCooledLocations -= byNumberOfSpaces;
            return true;
        }
        if (!isCooled & (byNumberOfSpaces <= this.freeNormalLocations)){
            this.freeNormalLocations -= byNumberOfSpaces;
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
