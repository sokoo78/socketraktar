package com.berraktar;

import java.io.Serializable;

public class Transactions implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = 2611041076912112709L;

    // Tranzakciók közös tulajdonságai
    private int transactionID;
    private Employee.TransactionType transactionType;
    private boolean isApproved;

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public int getTransactionID() {
        return this.transactionID;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved() {
        isApproved = true;
    }

    public Employee.TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Employee.TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}
