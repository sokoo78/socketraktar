package com.berraktar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public final class UserIO {

    // Igen vagy Nem szkenner
    public static boolean readBoolean () throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String userinput = "";
        // Csak igen-t vagy nem-et fogadunk el
        while (!userinput.equalsIgnoreCase("i") | !userinput.equalsIgnoreCase("n")){
            userinput = br.readLine().trim();
            if (userinput.equalsIgnoreCase("i")) return true;
            if (userinput.equalsIgnoreCase("n")) return false;
            System.out.println("Hibás válasz, próbáld újra (i/n): ");
        }
        return Boolean.parseBoolean(null);
    }

    // Dátum szkenner
    public static LocalDateTime readDate(boolean futureDateOnly) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = null;

        // Csak jó dátumot fogadunk el
        while (dateTime == null) {
            String userinput = br.readLine();
            try {
                dateTime = LocalDateTime.parse(userinput, format);
            } catch (DateTimeParseException e) {
                System.out.println("Hibás dátum formátum, próbáld újra: ");
            }
            // Ha futureDateOnly flag igaz, csak jövőbeni dűtumot fogadunk el
            if (dateTime!= null && futureDateOnly && Boolean.TRUE.equals(dateTime.isBefore(LocalDateTime.now()))){
                System.out.println("Csak jövőbeni dátum lehet, próbáld újra: ");
                dateTime = null;
            }
        }

        // Vágás 30 perces intervallumra
        dateTime = dateTime.truncatedTo(ChronoUnit.HOURS).plusMinutes(30 * (dateTime.getMinute() / 30));
        return dateTime;
    }

    // Dátum printer
    public static String printDate(LocalDateTime localDateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return formatter.format(localDateTime);
    }

    // Dátum ellenőrzése, hogy adott intervallumon belül van-e
    public static boolean DateisInRange(LocalDateTime startDate, LocalDateTime endDate, LocalDateTime actualDate) {
        return !(actualDate.isBefore(startDate) || actualDate.isAfter(endDate));
    }

    // Munkalap kitöltése a foglalási adatokkal
    public static Worksheet fillWorkSheet(Worksheet worksheet, Reservation reservation) {
        worksheet.setRenterID(reservation.RenterID);
        worksheet.setExternalID(reservation.PartNumber);
        worksheet.updateCooled(reservation.IsCooled);
        worksheet.setNumberOfPallets(reservation.Pallets);
        worksheet.setReservedDate(reservation.ReservationDate);
        return worksheet;
    }

    // Munkalap foglalási adatainak tömör (egy soros) kiíratása
    public static void printWorksheet(Worksheet worksheet) {
        System.out.print("\nID: " + worksheet.getTransactionID() + " (" + worksheet.isInitialized() + ")");
        System.out.print("\tBérlő: " + worksheet.getRenterID());
        System.out.print("\tCikk: " + worksheet.getExternalID());
        System.out.print("\tHűtött: " + worksheet.isCooled());
        System.out.print("\tRaklap: " + worksheet.getNumberOfPallets());
        System.out.print("\tIdő: " + UserIO.printDate(worksheet.getReservedDate()));
        System.out.print("\tFoglalás: ");
        System.out.print(worksheet.isApproved() ? "OK" : "NOK - " + worksheet.getTransactionMessage());
    }

}
