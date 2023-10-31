package org.mtcg.server;

import org.mtcg.app.controllers.UserController;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.json.JSONObject;
import org.mtcg.http.Method;

public class Router
{
    // Variablen für die Controller-Klassen
    private final UserController userController;

    // Konstruktor für die Router-Klasse
    public Router()
    {
        this.userController = new UserController();
    }

    // Methode zum Routen der HTTP-Requests
    public Response route(Request request)
    {
        // Method: POST, Path: /users
        if (request.getMethod() == Method.POST && "/users".equals(request.getPath()))
        {
            // Parse den request body als JSON und extrahiere Username und Password
            JSONObject json = new JSONObject(request.getBody());
            String username = json.getString("Username");
            String password = json.getString("Password");

            // Registriere den User
            boolean success = userController.registerUser(username, password);

            // Bei Erfolg senden Sie eine 200-Antwort zurück, ansonsten eine 500-Antwort
            if (success)
            {
                return new Response(HttpStatus.OK, ContentType.JSON, "User registered successfully");
            }
            else
            {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "User registration failed");
            }
        }
        // Wenn keine passende Route gefunden wird, senden Sie eine 404-Antwort zurück
        return new Response(HttpStatus.NOT_FOUND, ContentType.HTML, "Not Found");
    }
}
