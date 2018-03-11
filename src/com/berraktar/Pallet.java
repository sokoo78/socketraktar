package com.berraktar;

public class Pallet {
    private String internalId;
    private String externalId;
    private String renterID;

    public Pallet(String internalId, String externalId, String renterID) {
        this.internalId = internalId;
        this.externalId = externalId;
        this.renterID = renterID;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getRenterID() {
        return renterID;
    }

    public void setRenterID(String renterID) {
        this.renterID = renterID;
    }
}
