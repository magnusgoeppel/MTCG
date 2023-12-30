package org.mtcg.app.services;

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

public class TradingServices
{
    public TradingServices()
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

    public boolean deleteTrade(String tradeId)
    {
        // Löschen Sie das Handelsangebot
        try (Connection connection = DatabaseConnection.getConnection())
        {
            String query = "DELETE FROM trades WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query))
            {
                stmt.setString(1, tradeId);

                stmt.executeUpdate();
                return true;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
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

                    if (ownerId == userId)
                    {
                        return deleteTrade(tradeId);
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

