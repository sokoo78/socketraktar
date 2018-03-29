package com.berraktar;

import java.io.Serializable;

class MessageReport implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = 5843645352677089659L;

    // Jelentés típusok
    public enum ReportType {Renters, Worksheets, Locations, TerminalReservations, TerminalPallets, Receivings, Shipments, Protocols}

    // Jelentés tulajdonságai
    private ReportType reportType;
    private String reply;

    // Konstruktor
    MessageReport(ReportType reportType) {
        this.reportType = reportType;
    }

    // Getterek, setterek

    ReportType getReportType() {
        return reportType;
    }

    String getReply() {
        return reply;
    }

    void setReply(String reply) {
        this.reply = reply;
    }
}
