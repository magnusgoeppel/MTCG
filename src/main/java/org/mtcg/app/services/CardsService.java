package org.mtcg.app.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
    // Verbindung zur Datenbank
    private Connection connection;

    public CardsService()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    // Speicher der Karten des Benutzers
    public List<Card> getCardsForUser(int userId)
    {
        List<Card> cards = new ArrayList<>();

        String query = "SELECT * FROM cards WHERE id IN (SELECT card_id FROM user_cards WHERE user_id = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                Card card = new Card(rs.getString("id"),
                                     rs.getString("name"),
                                     rs.getInt("damage"));
                cards.add(card);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return cards;
    }

    // Speicher der Karten des Benutzers
    public List<Card> getDeckForUser(int userId)
    {
        List<Card> deck = new ArrayList<>();

        String query = "SELECT c.* FROM cards c " +
                       "JOIN deck_cards dc ON c.id = dc.card_id " +
                       "LEFT JOIN trades t ON c.id = t.offered_card_id " +
                       "WHERE dc.deck_id = (SELECT deck_id FROM users WHERE id = ?) " +
                       // Stellt sicher, dass die Karte nicht als Trade angeboten wird
                       "AND t.offered_card_id IS NULL";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                Card card = new Card(rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("damage"));
                deck.add(card);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return deck;
    }

    // Konvertieren in JSON von Karten und Deck
    public String convertToJson(List<Card> cards)
    {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cards);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // Konvertieren das Deck in Plain
    public String convertDeckToPlain(List<Card> deck)
    {
        StringBuilder sb = new StringBuilder();

        for (Card card : deck)
        {
            sb.append(card.getId()).append(", ").append
                    (card.getName()).append(", ").append
                    (card.getDamage()).append("\n");
        }
        return sb.toString();
    }

    // Überprüfen, ob die Anzahl der Karten im Deck gültig ist
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

    // Extrahieren der Karten-IDs aus dem Request Body
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

    // Überprüfen Sie, ob alle Karten dem Benutzer gehören und verfügbar sind
    public boolean areCardsValidAndBelongToUser(int userId, List<String> cardIds)
    {

        // Zählen Sie die Anzahl der Karten, die dem Benutzer gehören
        String query = "SELECT COUNT(*) AS card_count FROM user_cards WHERE user_id = ? AND card_id IN (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, userId);

            // Für alle Karten im Deck des Benutzers
            for (int i = 0; i < cardIds.size(); i++)
            {
                // Setzen Sie die Karten-ID als Parameter
                stmt.setString(i + 2, cardIds.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            // Überprüfen Sie, ob die Anzahl der Karten dem Deck entspricht
            if (rs.next())
            {
                int count = rs.getInt("card_count");
                return count == cardIds.size();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    // Konfigurieren des Decks des Benutzers
    public boolean configureDeckForUser(int userId, List<String> cardIds)
    {
        try
        {
            // Beginnen Sie eine Transaktion
            connection.setAutoCommit(false);

            // Löschen Sie zuerst alle aktuellen Karten im Deck des Benutzers
            String deleteQuery = "DELETE FROM deck_cards WHERE deck_id = (SELECT deck_id FROM users WHERE id = ?)";

            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery))
            {
                deleteStmt.setInt(1, userId);
                deleteStmt.executeUpdate();
            }
            catch (SQLException e)
            {
                connection.rollback();
                e.printStackTrace();
                return false;
            }

            // Fügen Sie jede Karte zum Deck des Benutzers hinzu
            String insertQuery = "INSERT INTO deck_cards (deck_id, card_id) VALUES ((SELECT deck_id FROM users WHERE id = ?), ?)";

            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery))
            {
                for (String cardId : cardIds)
                {
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, cardId);
                    insertStmt.executeUpdate();
                }
            }
            catch (SQLException e)
            {
                connection.rollback();
                e.printStackTrace();
                return false;
            }

            connection.commit();
            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}