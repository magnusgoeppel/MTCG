package org.mtcg.app.services;

import org.mtcg.app.models.Card;
import org.mtcg.app.models.TradeOffer;
import org.mtcg.database.DatabaseConnection;
// Jackson ObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TradingService
{
    public TradingService()
    {

    }

    public List<TradeOffer> getTrades()
    {
        List<TradeOffer> trades = new ArrayList<>();
        // Verbinden Sie sich mit der Datenbank und führen Sie eine Abfrage aus
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "SELECT Id, offered_card_id, minimum_damage, Type FROM trades";
            try (PreparedStatement stmt = connection.prepareStatement(query))
            {
                ResultSet rs = stmt.executeQuery();

                while (rs.next())
                {
                    TradeOffer trade = new TradeOffer();
                    trade.setId(rs.getString("Id"));
                    trade.setCardToTrade(rs.getString("offered_card_id"));
                    trade.setMinimumDamage(rs.getInt("minimum_damage"));
                    trade.setType(rs.getString("Type"));

                    trades.add(trade);
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();

        }
        return trades;
    }

    public String convertTradesToJson(List<TradeOffer> trades)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            return mapper.writeValueAsString(trades);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public TradeOffer convertJsonToTrade(String requestBody)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            TradeOffer trade = mapper.readValue(requestBody, TradeOffer.class);
            return trade;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean createTrade(TradeOffer trade, int userId)
    {
        // Erstellen Sie das Handelsangebot
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "INSERT INTO trades (id, user_id, offered_card_id, minimum_damage, type) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query))
            {
                stmt.setString(1, trade.getId());
                stmt.setInt(2, userId);
                stmt.setString(3, trade.getCardToTrade());
                stmt.setInt(4, trade.getMinimumDamage());
                stmt.setString(5, trade.getType());

                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public int deleteTrade(String tradeId)
    {
        // Überprüfen Sie zuerst, ob die Trade-ID existiert
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Überprüfen, ob die Trade-ID existiert
            String checkQuery = "SELECT COUNT(*) FROM trades WHERE id = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, tradeId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // Trade-ID existiert nicht, also gibt es nichts zu löschen
                    return 0;
                }
            }

            // Löschen Sie das Handelsangebot, wenn es existiert
            String deleteQuery = "DELETE FROM trades WHERE id = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                deleteStmt.setString(1, tradeId);
                int affectedRows = deleteStmt.executeUpdate();
                if(affectedRows > 0)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }


    public boolean checkOwnership(int userId, String tradeId)
    {
        // Überprüfen Sie, ob der Benutzer berechtigt ist, das Handelsangebot zu löschen
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "SELECT user_id FROM trades WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query))
            {
                stmt.setString(1, tradeId);

                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                {
                    int ownerId = rs.getInt("user_id");

                    // Wenn der Benutzer der Besitzer des Handelsangebots ist, geben Sie true zurück
                    if (ownerId == userId)
                    {
                        return true;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public String extractTradeIdFromPath(String path)
    {
        try {
            String[] pathParts = path.split("/");
            return pathParts[pathParts.length - 1]; // Der letzte Teil des Pfades ist die Trade-ID
        } catch (Exception e) {
            e.printStackTrace();
            return null; // oder werfen Sie eine geeignete Ausnahme
        }
    }

    public TradeOffer getTrade(String tradeId)
    {
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "SELECT Id, offered_card_id, minimum_damage, Type FROM trades WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query))
            {
                stmt.setString(1, tradeId);

                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                {
                    TradeOffer trade = new TradeOffer();
                    trade.setId(rs.getString("Id"));
                    trade.setCardToTrade(rs.getString("offered_card_id"));
                    trade.setMinimumDamage(rs.getInt("minimum_damage"));
                    trade.setType(rs.getString("Type"));

                    return trade;
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public boolean checkTradeExists(String tradeId)
    {
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "SELECT COUNT(*) FROM trades WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query))
            {
                stmt.setString(1, tradeId);

                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                {
                    int count = rs.getInt(1);
                    if (count == 1)
                    {
                        return true;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public String extractCardIdFromJson(String requestBody)
    {
        return requestBody.replace("\"", "");
    }

    public Card getCard(String cardId)
    {
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "SELECT Id, Name, Damage FROM cards WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query))
            {
                stmt.setString(1, cardId);

                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                {
                    Card card = new Card();
                    card.setId(rs.getString("Id"));
                    card.setName(rs.getString("Name"));
                    card.setDamage(rs.getInt("Damage"));

                    return card;
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public boolean executeTrade(TradeOffer trade, Card offeredCard, int userId)
    {

        try (Connection connection = DatabaseConnection.getConnection())
        {
            // Deaktivieren Sie die automatische Commit-Funktion, um eine Transaktion zu starten
            connection.setAutoCommit(false);

            // Löschen Sie die Karte aus den Benutzerkarten
            String deleteCardQuery = "DELETE FROM user_cards WHERE card_id = ?";
            try (PreparedStatement deleteCardStmt = connection.prepareStatement(deleteCardQuery)) {
                deleteCardStmt.setString(1, offeredCard.getId());
                deleteCardStmt.executeUpdate();
            }
            catch (SQLException e)
            {
                connection.rollback();
                e.printStackTrace();
                return false;
            }

            // Füge die Karte zu den Benutzerkarten hinzu
            String addCardQuery = "INSERT INTO user_cards (user_id, card_id) VALUES (?, ?)";
            try (PreparedStatement addCardStmt = connection.prepareStatement(addCardQuery)) {
                addCardStmt.setInt(1, userId);
                addCardStmt.setString(2, offeredCard.getId());
                addCardStmt.executeUpdate();
            }
            catch (SQLException e)
            {
                connection.rollback();
                e.printStackTrace();
                return false;
            }

            // Löschen Sie das Handelsangebot
            String deleteTradeQuery = "DELETE FROM trades WHERE id = ?";
            try (PreparedStatement deleteTradeStmt = connection.prepareStatement(deleteTradeQuery)) {
                deleteTradeStmt.setString(1, trade.getId());
                deleteTradeStmt.executeUpdate();
            }
            catch (SQLException e)
            {
                connection.rollback();
                e.printStackTrace();
                return false;
            }


            // Wenn alles erfolgreich war, committen Sie die Transaktion
            connection.commit();
            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkCardType(Card offeredCard, TradeOffer trade)
    {
        if (!offeredCard.getName().endsWith(trade.getType()) ||
                offeredCard.getDamage() < trade.getMinimumDamage())
        {
            return true;

        }
        return false;
    }

    public boolean checkCardLockedinDeck(Card offeredCard, int userId)
    {
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "SELECT deck_id FROM users WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query))
            {
                stmt.setInt(1, userId);

                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                {
                    int deckId = rs.getInt("deck_id");

                    String deckQuery = "SELECT COUNT(*) FROM deck_cards WHERE deck_id = ? AND card_id = ?";
                    try (PreparedStatement deckStmt = connection.prepareStatement(deckQuery))
                    {
                        deckStmt.setInt(1, deckId);
                        deckStmt.setString(2, offeredCard.getId());

                        ResultSet deckRs = deckStmt.executeQuery();
                        if (deckRs.next())
                        {
                            int count = deckRs.getInt(1);
                            if (count > 0)
                            {
                                // Karte ist im Deck gesperrt
                                return true;
                            }
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}

