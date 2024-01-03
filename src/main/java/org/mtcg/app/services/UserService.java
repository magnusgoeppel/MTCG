package org.mtcg.app.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Setter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.app.models.UserData;
import org.mtcg.database.DatabaseConnection;

import java.lang.reflect.Executable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Setter
public class UserService
{

    // Registrieren eines Benutzers
    public boolean registerUser(String username, String password)
    {
        try (Connection connection = DatabaseConnection.getConnection())
        {
            connection.setAutoCommit(false);

            // Erstelle ein neues Deck und erhalte die ID
            String insertDeckQuery = "INSERT INTO decks DEFAULT VALUES RETURNING id";
            int deckId;

            try (PreparedStatement insertDeckStmt = connection.prepareStatement(insertDeckQuery))
            {
                ResultSet deckRs = insertDeckStmt.executeQuery();

                if (!deckRs.next())
                {
                    connection.rollback();
                    return false;
                }
                deckId = deckRs.getInt(1);
            }
            catch (SQLException e)
            {
                connection.rollback();
                return false;
            }

            // Füge den neuen Benutzer in die users Tabelle ein und setze die deck_id
            String insertUserQuery = "INSERT INTO users (username, password, coins, deck_id) VALUES (?, ?, 20, ?)";
            try (PreparedStatement insertUserStmt = connection.prepareStatement(insertUserQuery)) {
                insertUserStmt.setString(1, username);
                insertUserStmt.setString(2, password);
                insertUserStmt.setInt(3, deckId);

                int userResult = insertUserStmt.executeUpdate();

                if (userResult == 1)
                {
                    // Hole die ID des Benutzers
                    int userId = getUserId(username, connection);

                    if(userId == -1)
                    {
                        connection.rollback();
                        return false;
                    }

                    // Rufe Methode zum Stats erstellen auf
                    boolean statsCreated = createStats(userId, connection);
                    boolean userAddedToScoreboard = addUserToScoreboard(userId, connection);

                    if(!statsCreated || !userAddedToScoreboard)
                    {
                        connection.rollback();
                        return false;
                    }

                    connection.commit();
                    return true;
                }
                else
                {
                    connection.rollback();
                    return false;
                }
            }
            catch (SQLException e)
            {
                connection.rollback();
                return false;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }

    }

    // Anmelden eines Benutzers
    public boolean loginUser(String username, String password)
    {
        try (Connection connection = DatabaseConnection.getConnection())
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
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Speichern Sie das Token für einen Benutzer in der Datenbank
    public boolean saveUserToken(String username, String token)
    {
        // Überprüfe, ob der Token bereits vergeben ist
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "SELECT * FROM users WHERE token = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, token);

            // Führe die SQL-Abfrage aus
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                return false;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }

        // Speichere den Token in der Datenbank
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "UPDATE users SET token = ? WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, token);
            stmt.setString(2, username);

            int result = stmt.executeUpdate();
            return result > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Extrahieren Sie die Benutzerdaten (Name, Bio, Image)
    public String getUserData(int userId)
    {
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "SELECT name, bio, image FROM users WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                UserData userData = new UserData();
                userData.setName(rs.getString("name"));
                userData.setBio(rs.getString("bio"));
                userData.setImage(rs.getString("image"));

                // Verwenden Sie Jackson, um das Objekt in einen JSON-String umzuwandeln
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(userData);
            }
            else
            {
                return null;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    // Aktualisieren Sie die Benutzerdaten (Name, Bio, Image)
    public boolean updateUser(int userId, String name, String bio, String image)
    {
        try (Connection connection = DatabaseConnection.getConnection())
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
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Löschen Sie das Token für einen Benutzer in der Datenbank
    public boolean deleteUserToken(int UserId)
    {
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "UPDATE users SET token = NULL WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, UserId);

            int result = stmt.executeUpdate();
            return result > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------ Hilfsfunktionen ------------------------------

    public int getUserId(String username, Connection connection)
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
        catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    // Holen Sie den Benutzernamen eines Benutzers
    public String getUsername(int userId)
    {
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "SELECT username FROM users WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                return rs.getString("username");
            }
            else
            {
                return null;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // Erstellen Sie die Stats für einen Benutzer
    public boolean createStats(int user_id, Connection connection)
    {
        try
        {
            String query = "INSERT INTO stats (user_id, elo, wins, losses) VALUES (?, 100, 0, 0)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, user_id);

            int result = stmt.executeUpdate();
            return result > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Fügen Sie einen Benutzer zum Scoreboard hinzu
    public boolean addUserToScoreboard(int user_id, Connection connection)
    {
        try
        {
            String query = "INSERT INTO scoreboards (user_id, place) VALUES (?, 1)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, user_id);

            int result = stmt.executeUpdate();
            return result > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}