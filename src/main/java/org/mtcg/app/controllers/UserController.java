package org.mtcg.app.controllers;

import org.json.JSONObject;
import org.mtcg.app.services.UserService;
import org.mtcg.app.services.authService;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;

public class UserController
{
    // Instanzen der Services
    private UserService userService;
    private authService authService;

    public UserController()
    {
        this.userService = new UserService();
        this.authService = new authService();
    }


    // Registrieren eines Benutzers
    public Response handleRegister(Request request)
    {
        // Extrahieren der Benutzerdaten aus dem Request
        JSONObject json = new JSONObject(request.getBody());

        String username = json.getString("Username");
        String password = json.getString("Password");

        // Registriere den Benutzer
        boolean success = userService.registerUser(username, password);

        // Gebe zurück, ob die Registrierung erfolgreich war
        if (success)
        {
            return new Response(HttpStatus.CREATED, ContentType.JSON, "User registered successfully");
        }
        else
        {
            return new Response(HttpStatus.CONFLICT, ContentType.JSON, "User registration failed");
        }
    }

    public Response handleLogin(Request request)
    {
        // Extrahieren der Benutzerdaten aus dem Request
        JSONObject json = new JSONObject(request.getBody());

        String username = json.getString("Username");
        String password = json.getString("Password");

        // Überprüfen der Benutzerdaten
        boolean validCredentials = userService.loginUser(username, password);

        // Speichern des Tokens für den Benutzer
        String token = username + "-mtcgToken";
        boolean tokenSaved = userService.saveUserToken(username, token);

        // Gebe zurück, ob die Anmeldung erfolgreich war
        if (validCredentials && tokenSaved)
        {
            return new Response(HttpStatus.OK, ContentType.JSON, "User logged in successfully");

        }
        else
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "User login failed");
        }
    }

    // UserData ausgeben (Name, Bio, Image)
    public Response handleGetUser(Request request)
    {
        int userId = authService.extractUserIdFromAuthHeader(request);

        if (userId == -1)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // {username} aus dem Request extrahieren
        String username = request.getPath().split("/")[2];

        // Extrahieren des Tokens und Entfernen des "-mtcgToken" Teils
        String token = request.getHeaders().get("Authorization").substring(7, request.getHeaders().get("Authorization").length() - 10);

        // Überprüfen, ob Username und Token übereinstimmen
        if(!username.equals(token))
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Extrahieren Sie die Benutzerdaten
        String userData = userService.getUserData(userId);

        // Wenn keine Benutzerdaten gefunden wurden, geben Sie eine Fehlermeldung zurück
        if(userData == null)
        {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "User not found");
        }
        // Ansonsten geben Sie die Benutzerdaten zurück
        return new Response(HttpStatus.OK, ContentType.JSON, userData);
    }

    // UserData aktualisieren (Name, Bio, Image)
    public Response handleUpdateUser(Request request)
    {
        // Extrahieren der userId aus dem Token
        int userId = authService.extractUserIdFromAuthHeader(request);

        if (userId == -1)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Hole den Benutzernamen aus dem Request
        String username = request.getPath().split("/")[2];

        // Extrahieren des Tokens und Entfernen des "-mtcgToken" Teils
        String token = request.getHeaders().get("Authorization").substring(7, request.getHeaders().get("Authorization").length() - 10);

        // Überprüfen, ob Username und Token übereinstimmen
        if(!username.equals(token))
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        JSONObject json = new JSONObject(request.getBody());
        String name = json.getString("Name");
        String bio = json.getString("Bio");
        String image = json.getString("Image");

        boolean success = userService.updateUser(userId, name, bio, image);

        // Gebe zurück, ob die Aktualisierung erfolgreich war
        if (success)
        {
            return new Response(HttpStatus.OK, ContentType.JSON, "User updated successfully");
        }
        else
        {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "User not found");
        }
    }

    // Ausloggen eines Benutzers
    public Response handleLogout(Request request)
    {
        // Extrahieren der userId aus dem Token
        int userId = authService.extractUserIdFromAuthHeader(request);

        if (userId == -1)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Hole den username aus der DB
        String username = userService.getUsername(userId);

        // Extrahieren des Tokens und Entfernen des "-mtcgToken" Teils
        String token = request.getHeaders().get("Authorization").substring(7, request.getHeaders().get("Authorization").length() - 10);

        // Überprüfen, ob Username und Token übereinstimmen
        if(!username.equals(token))
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Löschen des Tokens
        boolean success = userService.deleteUserToken(userId);

        // Gebe zurück, ob das Ausloggen erfolgreich war
        if (success)
        {
            return new Response(HttpStatus.OK, ContentType.JSON, "User logged out successfully");
        }
        else
        {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "User not found");
        }
    }
}

