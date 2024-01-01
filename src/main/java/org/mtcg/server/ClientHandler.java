package org.mtcg.server;

import org.mtcg.http.Method;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable
{

    // Variablen für den ClientHandler
    private final Socket clientSocket;
    private final Router router;

    // Konstruktor für den ClientHandler
    public ClientHandler(Socket socket)
    {
        this.clientSocket = socket;
        this.router = new Router();
    }

    // Methode zum Verarbeiten der HTTP-Requests
    @Override
    public void run()
    {
        // Erstellt einen BufferedReader zum Lesen der HTTP-Request-Zeilen
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream())
        {
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
            Map<String, String> headers = new HashMap<>();

            while (!(line = in.readLine()).isEmpty())
            {
                String[] headerParts = line.split(": ");

                if (headerParts.length == 2)
                {
                    headers.put(headerParts[0], headerParts[1]);
                    if ("Content-Length".equalsIgnoreCase(headerParts[0]))
                    {
                        contentLength = Integer.parseInt(headerParts[1].trim());
                    }
                }
            }
            // Liest die Query-Parameter und speichert sie in einer Map
            Map<String, String> queryParams = new HashMap<>();

            if (requestPath.contains("?"))
            {
                String[] pathParts = requestPath.split("\\?");
                requestPath = pathParts[0];
                String[] queryParts = pathParts[1].split("&");
                for (String queryPart : queryParts)
                {
                    String[] queryParamParts = queryPart.split("=");
                    if (queryParamParts.length == 2)
                    {
                        queryParams.put(queryParamParts[0], queryParamParts[1]);
                    }
                }
            }

            // Liest den Request-Body
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars, 0, contentLength);
            String requestBody = new String(bodyChars);

            // Erstellt ein Request-Objekt
            Request request = new Request(Method.valueOf(requestMethod), requestPath, requestVersion, requestBody, headers, queryParams);

            // Verwenden Sie den Router, um die Anfrage zu verarbeiten und eine Antwort zu erhalten
            Response response = router.route(request);
            // Baut die HTTP-Antwort
            String httpResponse = response.build();
            // Sendet die HTTP-Antwort
            out.write(httpResponse.getBytes(StandardCharsets.UTF_8));
            // Stellt sicher, dass die Antwort gesendet wird, bevor Sie fortfahren
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}