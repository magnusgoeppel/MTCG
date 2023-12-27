package org.mtcg.app.controllers;

import org.json.JSONObject;
import org.mtcg.app.services.UserService;
import org.mtcg.app.services.CommonService;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;

public class UserController
{
    private UserService userService;
    private CommonService commonService;

    public UserController()
    {
        this.userService = new UserService();
        this.commonService = new CommonService();
    }

    public Response handleRegister(Request request)
    {
        JSONObject json = new JSONObject(request.getBody());
        String username = json.getString("Username");
        String password = json.getString("Password");


        boolean success = userService.registerUser(username, password);

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
        JSONObject json = new JSONObject(request.getBody());
        String username = json.getString("Username");
        String password = json.getString("Password");

        boolean validCredentials = userService.loginUser(username, password);

        String token = username + "-mtcgToken";
        boolean tokenSaved = userService.saveUserToken(username, token);

        if (validCredentials && tokenSaved)
        {
            return new Response(HttpStatus.OK, ContentType.JSON, "User logged in successfully");

        }
        else
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "User login failed");
        }
    }

    public Response handleGetUser(Request request)
    {
        // Extrahieren des Authorization-Headers
        String authHeader = request.getHeaders().get("Authorization");


        int userId;
        try {
            userId = commonService.extractUserIdFromAuthHeader(authHeader);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // {username} und username müssen übereinstimmen
        String username = request.getPath().split("/")[2];

        // Extrahieren des Tokens und Entfernen des "-mtcgToken" Teils
        String token = request.getHeaders().get("Authorization").substring(7, request.getHeaders().get("Authorization").length() - 10);

        //check if username and token match
        if(!username.equals(token))
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Extrahieren Sie die Benutzerdaten
        String userData = userService.getUserData(userId);

        if(userData == null)
        {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "User not found");
        }
        return new Response(HttpStatus.OK, ContentType.JSON, userData);
    }

    public Response handleUpdateUser(Request request)
    {
        // Extrahieren des Authorization-Headers
        String authHeader = request.getHeaders().get("Authorization");

        int userId;
        try {
            userId = commonService.extractUserIdFromAuthHeader(authHeader);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // {username} und username müssen übereinstimmen
        String username = request.getPath().split("/")[2];

        // Extrahieren des Tokens und Entfernen des "-mtcgToken" Teils
        String token = request.getHeaders().get("Authorization").substring(7, request.getHeaders().get("Authorization").length() - 10);

        //check if username and token match
        if(!username.equals(token))
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        JSONObject json = new JSONObject(request.getBody());
        String name = json.getString("Name");
        String bio = json.getString("Bio");
        String image = json.getString("Image");

        boolean success = userService.updateUser(userId, name, bio, image);

        if (success)
        {
            return new Response(HttpStatus.OK, ContentType.JSON, "User updated successfully");
        }
        else
        {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "User not found");
        }
    }
}

