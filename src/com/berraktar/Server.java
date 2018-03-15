package com.berraktar;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static final int PORT = 4445;
    public Warehouse warehouse = new Warehouse(3000, 800, 9, 3);
    public Accounting accounting = new Accounting();

    public static void main(String[] args) throws IOException {
        new Server().runServer();
    }

    // Szerver indítása
    private void runServer() throws IOException {
        // Szabad lokációk beállítása
        this.warehouse.decreaseFreeCooledLocations(this.accounting.getTotalCooledReservations());
        this.warehouse.decreaseFreeNormalLocations(this.accounting.getTotalNormalReservations());

        // Socket megnyitása
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("A szerver elindult...");

        // Új szál indítása minden csatlakozó kliensnek
        while (true) {
            Socket socket = serverSocket.accept();
            new ServerThread(socket, warehouse, accounting).start();
        }
    }

}
