package org.mtcg.app.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.app.models.Card;
import org.mtcg.database.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PackageService
{
    private Connection connection;

    public PackageService()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean createPackage(List<Card> cards)
    {
        try {
            // Starte eine Transaktion
            connection.setAutoCommit(false);

            // Füge ein Package in die packages Tabelle ein und hole dir die ID
            String insertPackageSql = "INSERT INTO packages DEFAULT VALUES RETURNING id";

            try (PreparedStatement insertPackageStmt = connection.prepareStatement(insertPackageSql))
            {
                ResultSet packageResultSet = insertPackageStmt.executeQuery();

                if (!packageResultSet.next())
                {
                    connection.rollback();
                    return false;
                }
                int packageId = packageResultSet.getInt(1);
                System.out.println(packageId);

                // Füge alle Karten in die cards Tabelle ein und hole dir die IDs
                for (Card card : cards)
                {
                    String insertCardSql = "INSERT INTO cards (id, name, damage) VALUES (?, ?, ?) RETURNING id";

                    try (PreparedStatement insertCardStmt = connection.prepareStatement(insertCardSql))
                    {
                        insertCardStmt.setString(1, card.getId());
                        System.out.println(card.getId());
                        insertCardStmt.setString(2, card.getName());
                        insertCardStmt.setDouble(3, card.getDamage());
                        System.out.println(card.getDamage());
                        ResultSet cardResultSet = insertCardStmt.executeQuery();

                        if (!cardResultSet.next())
                        {
                            connection.rollback();
                            return false;
                        }
                        String cardId = cardResultSet.getString(1);

                        String insertPackageCardSql = "INSERT INTO package_cards (package_id, card_id) VALUES (?, ?)";

                        try (PreparedStatement insertPackageCardStmt = connection.prepareStatement(insertPackageCardSql))
                        {
                            insertPackageCardStmt.setInt(1, packageId);
                            insertPackageCardStmt.setString(2, cardId);
                            insertPackageCardStmt.executeUpdate();
                        }
                    }
                }

                // Committe die Transaktion
                connection.commit();
                return true;
            }
        }
        catch (SQLException e)
        {
            try
            {
                // Rollback der Transaktion
                connection.rollback();

            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;

        }
        finally
        {
            try
            {
                // Setze AutoCommit wieder auf true
                connection.setAutoCommit(true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    // Methode zum Kaufen eines Pakets
    public boolean acquirePackage(int userId)
    {
        try
        {
            connection.setAutoCommit(false);

            // Reduziere die Münzen des Benutzers um 5, wenn er genug Münzen hat
            String query = "UPDATE users SET coins = coins - 5 WHERE id = ? AND coins >= 5";

            try (PreparedStatement stmt = connection.prepareStatement(query))
            {

                stmt.setInt(1, userId);
                int result = stmt.executeUpdate();

                // Wenn der Benutzer genügend Münzen hat, fahren Sie fort
                if (result > 0)
                {
                    // Wählen Sie ein Paket aus (z.B. das neueste oder ein zufälliges)
                    int packageId = selectPackage();
                    // Abrufen der Karten des Pakets
                    List<String> cardIds = getCardsFromPackage(packageId);

                    // Aktualisieren der deck_cards Tabelle
                    for (String cardId : cardIds) {
                        // Fügen Sie die Karte zur user_cards Tabelle hinzu
                        String insertUserCardQuery = "INSERT INTO user_cards (user_id, card_id) VALUES (?, ?)";
                        try (PreparedStatement insertUserCardStmt = connection.prepareStatement(insertUserCardQuery)) {
                            insertUserCardStmt.setInt(1, userId);
                            insertUserCardStmt.setString(2, cardId);
                            insertUserCardStmt.executeUpdate();
                        }
                    }

                    deletePackageAndCards(packageId);

                    connection.commit();
                    return true;
                }
                else
                {
                    connection.rollback();
                    return false;
                }
            }
        }
        catch (SQLException e)
        {
            try
            {
                connection.rollback();
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
        finally
        {
            try
            {
                connection.setAutoCommit(true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    private int selectPackage() throws SQLException
    {
        String query = "SELECT id FROM packages ORDER BY id LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                return rs.getInt("id");
            } else {
                throw new SQLException("No packages found");
            }
        }
    }


    private List<String> getCardsFromPackage(int packageId) throws SQLException
    {
        List<String> cardIds = new ArrayList<>();

        String query = "SELECT card_id FROM package_cards WHERE package_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, packageId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                cardIds.add(rs.getString("card_id"));
            }
        }
        return cardIds;
    }


    private int getDeckIdForUser(int userId) throws SQLException
    {
        String query = "SELECT deck_id FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                return rs.getInt("deck_id");
            }
            else
            {
                throw new SQLException("User not found or deck not set");
            }
        }
    }

    private void deletePackageAndCards(int packageId) throws SQLException
    {
        // Löschen Sie alle Karten aus der package_cards Tabelle, die zum Paket gehören
        String deleteCardsQuery = "DELETE FROM package_cards WHERE package_id = ?";
        try (PreparedStatement deleteCardsStmt = connection.prepareStatement(deleteCardsQuery))
        {
            deleteCardsStmt.setInt(1, packageId);
            deleteCardsStmt.executeUpdate();
        }

        // Löschen Sie das Paket aus der packages Tabelle
        String deletePackageQuery = "DELETE FROM packages WHERE id = ?";

        try (PreparedStatement deletePackageStmt = connection.prepareStatement(deletePackageQuery))
        {
            deletePackageStmt.setInt(1, packageId);
            deletePackageStmt.executeUpdate();
        }
    }
    private int countCardsInDeck(int userId) throws SQLException
    {
        String query = "SELECT COUNT(*) FROM deck_cards WHERE deck_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, getDeckIdForUser(userId));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // Methode zum Konvertieren eines JSON-Strings in eine Liste von Kartenobjekten
    public List<Card> convertJsonToCards(String json)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (IOException e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean checkCardsExistence(List<Card> cards)
    {
        String query = "SELECT id FROM cards WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query))
        {
            for (Card card : cards)
            {
                stmt.setString(1, card.getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                {
                    return true; // Mindestens eine Karte existiert bereits
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getAdminToken()
    {
        String adminToken = null;
        String query = "SELECT token FROM users WHERE username = 'admin'";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query))
        {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                adminToken = rs.getString("token");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return adminToken;
    }

    // Methode zum Überprüfen, ob ein Benutzer genügend Münzen hat
    public boolean checkCoins(int userId)
    {
        String query = "SELECT coins FROM users WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                int coins = rs.getInt("coins");
                return coins >= 5;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    // Methode zum Überprüfen, ob ein Paket verfügbar ist
    public boolean checkPackageAvailable()
    {
        String query = "SELECT COUNT(*) AS package_count FROM packages";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query))
        {
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                int packageCount = rs.getInt("package_count");
                return packageCount > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}