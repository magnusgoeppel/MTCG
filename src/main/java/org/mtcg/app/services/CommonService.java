package org.mtcg.app.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.app.models.Card;
import org.mtcg.database.DatabaseConnection;
import org.mtcg.server.Request;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CommonService
{
    private Connection connection;

    public CommonService()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    public int extractUserIdFromAuthHeader(Request request)
    {
        String authHeader = request.getHeaders().get("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            return -1;
        }

        String token = authHeader.substring(7); // Entfernen von "Bearer "

        int userId;

        try
        {
            userId = getUserIdFromToken(token);
        }
        catch (Exception e)
        {
           return -1;
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

}
