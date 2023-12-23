package org.mtcg.app.controllers;

import org.json.JSONObject;
import org.mtcg.app.services.UserService;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;

public class UserController
{
    private UserService userService;

    public UserController()
    {
        this.userService = new UserService();
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
}

