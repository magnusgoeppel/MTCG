package org.mtcg.app.services;

import org.mtcg.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CommonService
{
    private Connection connection;

    public CommonService()
    {
        this.connection = DatabaseConnection.getConnection();
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
