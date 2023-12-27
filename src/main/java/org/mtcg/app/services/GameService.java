package org.mtcg.app.services;

import org.json.JSONObject;
import org.mtcg.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
}
