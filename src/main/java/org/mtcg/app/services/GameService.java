package org.mtcg.app.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mtcg.app.models.Stats;
import org.mtcg.app.models.User;
import org.mtcg.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameService
{
    private Connection connection;

    public GameService()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    public String getStats(int userId)
    {
        // Hole den Name aus der users Tabelle
        String query = "SELECT username FROM users WHERE id = ?";
        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                String username = rs.getString("username");

                // Hole die Stats aus der stats Tabelle
                query = "SELECT * FROM stats WHERE user_id = ?";
                stmt = connection.prepareStatement(query);
                stmt.setInt(1, userId);
                rs = stmt.executeQuery();

                if (rs.next())
                {
                    // Erstelle ein JSON-Objekt mit den Stats
                    JSONObject stats = new JSONObject();
                    stats.put("Username", username);
                    stats.put("Elo", rs.getInt("elo"));
                    stats.put("Wins", rs.getInt("wins"));
                    stats.put("Losses", rs.getInt("losses"));

                    return stats.toString();
                }
                else
                {
                    return null;
                }
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

    public List<Stats> getScoreboard() {
        List<Stats> scoreboard = new ArrayList<>();
        // Verbinden Sie sich mit der Datenbank und führen Sie eine Abfrage aus
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT elo, wins, losses, username FROM stats INNER JOIN users ON stats.user_id = users.id ORDER BY elo DESC";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();

                while (rs.next())
                {
                    // Extrahieren Sie die Werte aus dem ResultSet
                    String name = rs.getString("username");
                    int elo = rs.getInt("elo");
                    int wins = rs.getInt("wins");
                    int losses = rs.getInt("losses");

                    // Erstellen Sie ein neues Stats-Objekt mit den extrahierten Werten
                    Stats stats = new Stats(elo, wins, losses, name);

                    // Fügen Sie das Stats-Objekt zur Scoreboard-Liste hinzu
                    scoreboard.add(stats);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Behandeln Sie den Fehler angemessen
        }
        return scoreboard;
    }

    // convert scoreboard to json
    public String convertScoreboardToJson(List<Stats> scoreboard)
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(scoreboard);
        } catch (Exception e)
        {
            e.printStackTrace();
            return "[]"; // oder eine angemessene Fehlerbehandlung
        }
    }
}
