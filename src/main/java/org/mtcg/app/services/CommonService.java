package org.mtcg.app.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.app.models.Card;
import org.mtcg.database.DatabaseConnection;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CommonService
{
    private Connection connection;

    public CommonService()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    public int extractUserIdFromAuthHeader(String authHeader) throws Exception
    {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Unauthorized: No token provided");
        }

        String token = authHeader.substring(7); // Entfernen von "Bearer "

        int userId;

        try
        {
            userId = getUserIdFromToken(token);
        }
        catch (Exception e)
        {
            throw new Exception("Unauthorized: Invalid token");
        }
        return userId;
    }

    public int getUserIdFromToken(String token)
    {
        int userId = -1;
        String query = "SELECT id FROM users WHERE token = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setString(1, token);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next())
            {
                userId = resultSet.getInt("id");
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return userId;
    }

    public String convertToJson(List<Card> cards)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            return mapper.writeValueAsString(cards);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


}
