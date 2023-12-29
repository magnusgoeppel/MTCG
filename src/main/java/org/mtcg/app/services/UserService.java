package org.mtcg.app.services;

import org.json.JSONObject;
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
            String insertUserQuery = "INSERT INTO users (username, password, coins, deck_id) VALUES (?, ?, 20, ?)";
            try (PreparedStatement insertUserStmt = connection.prepareStatement(insertUserQuery)) {
                insertUserStmt.setString(1, username);
                insertUserStmt.setString(2, password);
                insertUserStmt.setInt(3, deckId); // Setze die deck_id auf die ID des neu erstellten Decks

                int userResult = insertUserStmt.executeUpdate();
                if (userResult == 1)
                {
                    // Get user_id
                    int userId = getUserId(username);

                    if(userId == -1)
                    {
                        connection.rollback();
                        return false;
                    }

                    // Rufe Methode zum Stats erstellen auf
                    boolean statsCreated = createStats(userId);
                    boolean userAddedToScoreboard = addUserToScoreboard(userId);

                    if(!statsCreated || !userAddedToScoreboard)
                    {
                        connection.rollback();
                        return false;
                    }

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
                JSONObject userData = new JSONObject();
                userData.put("Name", rs.getString("name"));
                userData.put("Bio", rs.getString("bio"));
                userData.put("Image", rs.getString("image"));

                return userData.toString();
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

    // Erstellen Sie die Stats für einen Benutzer
    public boolean createStats(int user_id)
    {
        try
        {
            String query = "INSERT INTO stats (user_id, elo, wins, losses) VALUES (?, 100, 0, 0)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, user_id);

            int result = stmt.executeUpdate();
            return result > 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Fügen Sie einen Benutzer zum Scoreboard hinzu
    public boolean addUserToScoreboard(int user_id)
    {
        try
        {
            String query = "INSERT INTO scoreboards (user_id, score) VALUES (?, 0)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, user_id);

            int result = stmt.executeUpdate();
            return result > 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private int getUserId(String username)
    {
        try
        {
            String query = "SELECT id FROM users WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                return rs.getInt("id");
            }
            else
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }
}
