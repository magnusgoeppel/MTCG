package org.mtcg.app.services;

import org.mtcg.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService
{
    private Connection connection;

    public UserService()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    // Registrieren eines Benutzers
    public boolean registerUser(String username, String password) {
        try {
            connection.setAutoCommit(false);

            // Erstelle ein neues Deck und erhalte die ID
            String insertDeckQuery = "INSERT INTO decks DEFAULT VALUES RETURNING id";
            int deckId;
            try (PreparedStatement insertDeckStmt = connection.prepareStatement(insertDeckQuery)) {
                ResultSet deckRs = insertDeckStmt.executeQuery();
                if (!deckRs.next()) {
                    connection.rollback();
                    return false;
                }
                deckId = deckRs.getInt(1);
            }

            // Füge den neuen Benutzer in die users Tabelle ein und setze die deck_id
            String insertUserQuery = "INSERT INTO users (username, password, coins, elovalue, deck_id) VALUES (?, ?, 20, 100, ?)";
            try (PreparedStatement insertUserStmt = connection.prepareStatement(insertUserQuery)) {
                insertUserStmt.setString(1, username);
                insertUserStmt.setString(2, password);
                insertUserStmt.setInt(3, deckId); // Setze die deck_id auf die ID des neu erstellten Decks

                int userResult = insertUserStmt.executeUpdate();
                if (userResult == 1) {
                    connection.commit();
                    return true; // Benutzer erfolgreich registriert
                } else {
                    connection.rollback();
                    return false; // Fehler beim Einfügen des Benutzers
                }
            }
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    // Extrahieren Sie die Benutzerdaten (Name, Bio, Image)
    public String getUserData(int userId)
    {
        try
        {
            String query = "SELECT name, bio, image FROM users WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                String name = rs.getString("name");
                String bio = rs.getString("bio");
                String image = rs.getString("image");

                return "{\"Name\":\"" + name + "\",\"Bio\":\"" + bio + "\",\"Image\":\"" + image + "\"}";
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // Aktualisieren Sie die Benutzerdaten (Name, Bio, Image)
    public boolean updateUser(int userId, String name, String bio, String image)
    {
        try
        {
            String query = "UPDATE users SET name = ?, bio = ?, image = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, bio);
            stmt.setString(3, image);
            stmt.setInt(4, userId);

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
