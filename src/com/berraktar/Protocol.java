package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;

class Protocol extends Message implements Serializable {

    // Szerializációhoz kell
    private static final long serialVersionUID = 6186375378188282034L;

    // Jegyzőkönyv adatai
    private String Employee;
    private LocalDateTime Date;

    // Konstruktor
    Protocol(int transactionID, LocalDateTime transactionDate) {
        super(transactionID);
        this.Date = transactionDate;
    }

    // Getterek, setterek

    String getEmployee() {
        return Employee;
    }

    void setEmployee(String employee) {
        Employee = employee;
    }

    LocalDateTime getDate() {
        return Date;
    }

    void setDate(LocalDateTime date) {
        Date = date;
    }
}
