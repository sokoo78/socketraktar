// Socket szerver
package com.berraktar;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    static final int PORT = 4445;
    private static boolean isRunning;
    private Warehouse warehouse = new Warehouse(3000, 800, 9, 3);
    private Accounting accounting = new Accounting();


    public static void main(String[] args) throws IOException {
        isRunning = false;
        new Server().runServer();
    }

    // Szerver indítása
    private void runServer() throws IOException {
        // Szabad lokációk beállítása
        this.warehouse.decreaseFreeCooledLocations(this.accounting.getTotalCooledReservations());
        this.warehouse.decreaseFreeNormalLocations(this.accounting.getTotalNormalReservations());

        // Socket megnyitása
        ServerSocket serverSocket = new ServerSocket(PORT);
        isRunning = true;
        System.out.println("A szerver elindult...");

        // Új szál indítása minden csatlakozó kliensnek
        while (isRunning) {
            Socket socket = serverSocket.accept();
            new ServerThread(socket, warehouse, accounting).start();
        }
    }

}
