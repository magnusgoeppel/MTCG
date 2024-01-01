package org.mtcg.app.services;

import org.mtcg.database.DatabaseConnection;
import org.mtcg.server.Request;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class authService
{
    // Verbindung zur Datenbank
    private Connection connection;

    public authService()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    // Extrahiert den Benutzernamen aus dem Token
    public int extractUserIdFromAuthHeader(Request request)
    {
        String authHeader = request.getHeaders().get("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            return -1;
        }

        // Entferne das "Bearer " aus dem Token
        String token = authHeader.substring(7);

        int userId = -1;

        String query = "SELECT id FROM users WHERE token = ?";

         try(PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setString(1, token);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next())
            {
                userId = resultSet.getInt("id");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return userId;
    }
}
