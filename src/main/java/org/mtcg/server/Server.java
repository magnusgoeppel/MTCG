package org.mtcg.server;

import lombok.Getter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Getter
public class Server
{
    // Server Port
    private static final int PORT = 10001;

    // Server starten
    public void start()
    {
            // Versucht einen ServerSocket auf dem Port 7777 zu erstellen
            try (ServerSocket serverSocket = new ServerSocket(PORT))
            {
                System.out.println("Server started on port " + PORT + "\n");

                // Wartet auf eingehende Verbindungen und startet für jede eine neue ClientHandler-Instanz
                while (true)
                {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            }
            // Fehlerbehandlung, wenn der ServerSocket nicht erstellt werden konnte
            catch (IOException e)
            {
                e.printStackTrace();
            }
    }
}