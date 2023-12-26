package org.mtcg.app.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.app.models.Card;
import org.mtcg.database.DatabaseConnection;

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
                                     rs.getDouble("damage"));
                cards.add(card);
            }
        }
        return cards;
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
                        rs.getDouble("damage"));
                deck.add(card);
            }
        }
        return deck;
    }

    public List<String> extractCardIdsFromRequestBody(String requestBody)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            List<String> cardIds = mapper.readValue(requestBody, new TypeReference<>(){});
            return cardIds;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean configureDeckForUser(int userId, List<String> cardIds)
    {

        Connection connection = null;

        try
        {
            connection = DatabaseConnection.getConnection();
            // Beginnen Sie eine Transaktion
            connection.setAutoCommit(false);

            // Löschen Sie zuerst alle aktuellen Karten im Deck des Benutzers
            String deleteQuery = "DELETE FROM deck_cards WHERE deck_id = (SELECT deck_id FROM users WHERE id = ?)";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, userId);
                deleteStmt.executeUpdate();
            }

            // Fügen Sie jede Karte zum Deck des Benutzers hinzu
            String insertQuery = "INSERT INTO deck_cards (deck_id, card_id) VALUES ((SELECT deck_id FROM users WHERE id = ?), ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                for (String cardId : cardIds) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, cardId);
                    insertStmt.executeUpdate();
                }
            }

            // Commit der Transaktion
            connection.commit();
            return true; // Deck erfolgreich konfiguriert
        } catch (SQLException e) {
            e.printStackTrace();
            // Rollback im Fehlerfall
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean areCardsValidAndBelongToUser(int userId, List<String> cardIds) throws SQLException
    {
        // Erstellen Sie eine SQL-Abfrage, um zu überprüfen, ob alle Karten-IDs dem Benutzer gehören
        // und in der Tabelle 'user_cards' oder einer ähnlichen Tabelle vorhanden sind.
        String query = "SELECT COUNT(*) AS card_count FROM user_cards WHERE user_id = ? AND card_id IN (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            for (int i = 0; i < cardIds.size(); i++) {
                stmt.setString(i + 2, cardIds.get(i)); // Setzen der Karten-IDs in die Abfrage
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("card_count");
                return count == cardIds.size(); // Überprüfen, ob die Anzahl der Karten, die dem Benutzer gehören, gleich der Anzahl der Karten im Deck ist
            }
        }
        return false; // Standardmäßig false zurückgeben, wenn die Abfrage nicht erfolgreich war oder keine Karten gefunden wurden
    }

    public boolean isDeckSizeValid(String requestBody)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            List<String> cardIds = mapper.readValue(requestBody, new TypeReference<>(){});
            return cardIds.size() == 4;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
