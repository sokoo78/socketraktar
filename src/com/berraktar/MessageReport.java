package com.berraktar;

import java.io.Serializable;

public class MessageReport implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = 5843645352677089659L;

    // Jelentés típusok
    public enum ReportType {Renters, Worksheets, Locations, Terminals, Receivings, Shipments}

    // Jelentés tulajdonságai
    private ReportType reportType;
    private String reply;

    // Konstruktor
    MessageReport(ReportType reportType) {
        this.reportType = reportType;
    }

    // Getterek, setterek

    public ReportType getReportType() {
        return reportType;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
