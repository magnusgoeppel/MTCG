package org.mtcg.app.services;

import org.mtcg.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserService
{
    private Connection connection;

    public UserService()
    {
        this.connection = DatabaseConnection.getConnection();
    }
    // Registrieren eines Benutzers
    public boolean registerUser(String username, String password)
    {
        try
        {
            // Überprüfen, ob der Benutzername bereits vergeben ist
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            // Führe die SQL-Abfrage aus
            int result = stmt.executeUpdate();

            return result > 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Anmelden eines Benutzers
    public boolean loginUser(String username, String password)
    {
        try
        {
            // Überprüfen, ob der Benutzername bereits vergeben ist
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            // Führe die SQL-Abfrage aus
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Speichern Sie das Token für einen Benutzer in der Datenbank
    public boolean saveUserToken(String username, String token)
    {
        try
        {
            String query = "UPDATE users SET token = ? WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, token);
            stmt.setString(2, username);

            int result = stmt.executeUpdate();
            return result > 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
