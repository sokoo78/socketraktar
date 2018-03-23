package com.berraktar;

import java.util.Map;

public final class Reporting {


    // TODO: bérlők jelentés
    public static synchronized ReportMessage RenterReport(ReportMessage reportMessage, Accounting accounting) {
        StringBuilder reply = new StringBuilder();
        Map<String, Renter> renters = accounting.getRenters();

        // Fejléc
        reply.append("\nID\t\t").append("Név\t\t\t\t").append("Normál lokációk\t\t");
        reply.append("Hűtött lokációk\t\t").append("Logisztikai műveletek\n");

        // Bérlők listája
        for (Map.Entry<String, Renter> entry : renters.entrySet()){
            Renter value = entry.getValue();
            reply.append(value.getCode()).append("\t").append(value.getName()).append("\t\t");
            reply.append(value.getRentedNormalLocations()).append("\t\t\t\t\t");
            reply.append(value.getRentedCooledLocations()).append("\t\t\t\t\t").append(value.getNumberOfLogisticsOperations());
            reply.append("\n");
        }

        reportMessage.setReply(reply.toString());
        return reportMessage;
    }
}
