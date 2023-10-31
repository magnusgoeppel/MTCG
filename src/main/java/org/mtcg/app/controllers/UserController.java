package org.mtcg.app.controllers;

import org.mtcg.app.models.User;
import org.mtcg.database.DatabaseConnection;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserController
{

    private Connection connection;

    public UserController()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    // Register a new user
    public boolean registerUser(String username, String password)
    {
        try {
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password); // In einer echten Anwendung sollte das Passwort gehasht und gesalzen werden
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Login a user
    public String loginUser(String username, String password)
    {
        try {
            String query = "SELECT token FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password); // In einer echten Anwendung sollte das Passwort gehasht und gesalzen werden
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("token");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
