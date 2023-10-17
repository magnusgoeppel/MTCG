package org.mtcg.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable
{
    private final Socket clientSocket;

    public ClientHandler(Socket socket)
    {
        this.clientSocket = socket;
    }
    @Override
    public void run()
    {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream())
        {

            // Lesen Sie die Anforderung des Clients
            String line;

            while (!(line = in.readLine()).isEmpty())
            {
                System.out.println(line);
            }

            // Senden Sie eine einfache Antwort
            String httpResponse = "HTTP/1.1 200 OK\r\n\r\nHello World!";
            out.write(httpResponse.getBytes(StandardCharsets.UTF_8));

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
