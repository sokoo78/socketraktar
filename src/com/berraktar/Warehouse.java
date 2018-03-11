package com.berraktar;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

public final class Warehouse implements Serializable {
    private static final long serialVersionUID = 1041441426752225702L;
    private static final int maxNormalLocation = 3000;   // Normál lokációk maximális száma
    private static final int maxCooledLocation = 800;   // Hűtött lokációk maximális száma
    private static final int maxNormalTerminal = 9;   // Normál terminálok maximális száma
    private static final int maxCooledTerminal = 3;   // Hűtött terminálok maximális száma
    private static Map<Integer,Location> normalLocations;   // Normál lokáció objektumok
    private static Map<Integer,Location> cooledLocations;   // Hűtött lokáció objektumok
    private static Map<Integer,Terminal> normalTerminals;   // Normál terminál objektumok
    private static Map<Integer,Terminal> cooledTerminals;   // Hűtött terminál objektumok
    private static Map<Integer,Worksheet> worksheets;       // Munkalapok

    // TODO: Meg tudod nézni, van-e annyi szabad terminál
    public Terminal ReserveTerminal(LocalDateTime date, boolean isCooled){
        if (isCooled) {

        }
        else {

        }


        return null;
    }
}
