package org.mtcg.app.services;

import org.mtcg.app.models.Card;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mtcg.database.DatabaseConnection;

public class DeckService
{
    private Connection connection;

    public DeckService()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    public List<Card> getDeckForUser(int userId) throws SQLException
    {
        List<Card> deck = new ArrayList<>();
        String query = "SELECT * FROM cards WHERE id IN (SELECT card_id FROM deck_cards WHERE deck_id = (SELECT deck_id FROM users WHERE id = ?))";

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
                deck.add(card);
            }
        }
        return deck;
    }
}
