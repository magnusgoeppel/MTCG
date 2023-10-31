package org.mtcg.server;

import org.mtcg.http.Method;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable
{

    // Variablen für den ClientHandler
    private final Socket clientSocket;
    private final Router router;

    // Konstruktor für den ClientHandler
    public ClientHandler(Socket socket)
    {;
        this.clientSocket = socket;
        this.router = new Router();
    }

    // Methode zum Verarbeiten der HTTP-Requests
    @Override
    public void run()
    {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            // Variablen für die HTTP-Request-Parameter
            String line;
            String requestMethod = null;
            String requestPath = null;
            String requestVersion = "HTTP/1.1";
            int contentLength = 0;

            // Liest die HTTP-Request-Zeile und extrahiert Methode und Pfad
            if ((line = in.readLine()) != null)
            {
                String[] requestParts = line.split(" ");

                if (requestParts.length > 2)
                {
                    requestMethod = requestParts[0];
                    requestPath = requestParts[1];
                    requestVersion = requestParts[2];
                }
            }

            // Liest die Header-Zeilen und sucht nach Content-Length
            while (!(line = in.readLine()).isEmpty())
            {
                if (line.startsWith("Content-Length: ")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            // Liest den Request-Body
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars, 0, contentLength);
            String requestBody = new String(bodyChars);

            // Erstellt ein Request-Objekt
            Request request = new Request(Method.valueOf(requestMethod), requestPath, requestVersion, requestBody);

            // Verwenden Sie den Router, um die Anfrage zu verarbeiten und eine Antwort zu erhalten
            Response response = router.route(request);
            String httpResponse = response.build();
            out.write(httpResponse.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
