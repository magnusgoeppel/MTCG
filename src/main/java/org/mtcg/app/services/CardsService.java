package org.mtcg.app.services;

import org.mtcg.app.models.Card;
import org.mtcg.database.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CardsService
{
    private Connection connection;

    public CardsService()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    public String convertCardsToJson(List<Card> cards)
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


    public List<Card> getCardsForUser(int userId) throws SQLException
    {
        List<Card> cards = new ArrayList<>();
        String query = "SELECT * FROM cards WHERE id IN (SELECT card_id FROM user_cards WHERE user_id = ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                Card card = new Card(rs.getString("id"),
                                     rs.getString("name"),
                                     rs.getDouble("damage"),
                                     rs.getString("elementType"),
                                     rs.getString("type"));
                cards.add(card);
            }
        }

        return cards;
    }
}
