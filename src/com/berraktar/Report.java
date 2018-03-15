package com.berraktar;

import java.io.Serializable;

public class Report implements Serializable {
    // Szerializációhoz kell
    private static final long serialVersionUID = 5843645352677089659L;

    // Jelentés típusok
    public enum ReportType {Renters, Worksheets, Locations}

    // Jelentés tulajdonságai
    private ReportType report;
    private String reply;

    // Konstruktor
    public Report(ReportType report) {
        this.report = report;
    }

    // Getterek, setterek

    public ReportType getReport() {
        return report;
    }

    public void setReport(ReportType report) {
        this.report = report;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
