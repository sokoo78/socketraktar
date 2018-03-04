package com.berraktar;

// Socket szerver

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static final int PORT = 4444;
    public static void main(String[] args) throws IOException {
        new Server().runServer();
    }

    private void runServer() throws IOException {
        // Szerver indítása
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("A szerver elindult...");

        // TODO: Ide azért lehet hogy nem ártana valamilyen kilépési feltétel + hibakezelés
        // Új szál indítása minden csatlakozó kliensnek
        while (true) {
            Socket socket = serverSocket.accept();
            new ServerThread(socket).start();
        }

    }

}
