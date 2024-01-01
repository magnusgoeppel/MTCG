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
    // Verbindung zur Datenbank
    private Connection connection;

    public PackageService()
    {
        this.connection = DatabaseConnection.getConnection();
    }


    // Methode zum Abrufen des Tokens des Admins
    public String getAdminToken()
    {
        String adminToken = null;
        String query = "SELECT token FROM users WHERE username = 'admin'";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                adminToken = rs.getString("token");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return adminToken;
    }

    // Konvertieren der Kartenliste in einen JSON-String
    public List<Card> convertJsonToCards(String json)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            return mapper.readValue(json, new TypeReference<>() {});
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // überprüfen, ob die Karten bereits verfügbar sind
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

    // Erstellen eines Pakets und der Karten
    public boolean createPackage(List<Card> cards)
    {
        try
        {
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

                // Füge alle Karten in die cards Tabelle ein und hole die IDs
                for (Card card : cards)
                {
                    String insertCardSql = "INSERT INTO cards (id, name, damage) VALUES (?, ?, ?) RETURNING id";

                    try (PreparedStatement insertCardStmt = connection.prepareStatement(insertCardSql))
                    {
                        insertCardStmt.setString(1, card.getId());
                        insertCardStmt.setString(2, card.getName());
                        insertCardStmt.setDouble(3, card.getDamage());
                        ResultSet cardResultSet = insertCardStmt.executeQuery();

                        if (!cardResultSet.next())
                        {
                            connection.rollback();
                            return false;
                        }
                        String cardId = cardResultSet.getString(1);

                        // Füge die Karte in die package_cards Tabelle ein
                        String insertPackageCardSql = "INSERT INTO package_cards (package_id, card_id) VALUES (?, ?)";

                        try (PreparedStatement insertPackageCardStmt = connection.prepareStatement(insertPackageCardSql))
                        {
                            insertPackageCardStmt.setInt(1, packageId);
                            insertPackageCardStmt.setString(2, cardId);
                            insertPackageCardStmt.executeUpdate();
                        }
                        catch (SQLException e)
                        {
                            e.printStackTrace();
                            connection.rollback();
                            return false;
                        }
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                        connection.rollback();
                        return false;
                    }
                }
                connection.commit();
                return true;
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                connection.rollback();
                return false;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Überprüfen, ob ein Benutzer genügend Münzen hat
    public boolean checkCoins(int userId)
    {
        String query = "SELECT coins FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
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

    // Überprüfen, ob ein Paket verfügbar ist
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

    // Paket kaufen
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

                // Wenn der Benutzer genügend Münzen hat
                if (result > 0)
                {
                    // Paket auswählen
                    int packageId = selectPackage();

                    if(packageId > 0)
                    {
                        // Hole alle Karten aus dem Paket
                        List<String> cardIds = getCardsFromPackage(packageId);

                        // Aktualisieren der deck_cards Tabelle
                        for (String cardId : cardIds)
                        {
                            String insertUserCardQuery = "INSERT INTO user_cards (user_id, card_id) VALUES (?, ?)";

                            try (PreparedStatement insertUserCardStmt = connection.prepareStatement(insertUserCardQuery)) {
                                insertUserCardStmt.setInt(1, userId);
                                insertUserCardStmt.setString(2, cardId);
                                insertUserCardStmt.executeUpdate();
                            }
                            catch (SQLException e)
                            {
                                e.printStackTrace();
                                connection.rollback();
                                return false;
                            }
                        }

                        // Löschen des Pakets und der Karten
                        if(deletePackage(packageId))
                        {
                            connection.commit();
                            return true;
                        }
                        else
                        {
                            connection.rollback();
                            return false;
                        }
                    }
                    else
                    {
                        connection.rollback();
                        return false;
                    }
                }
                else
                {
                    connection.rollback();
                    return false;
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                connection.rollback();
                return false;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------ Hilfsfunktionen ------------------------------

    private int selectPackage()
    {
        String query = "SELECT id FROM packages ORDER BY id LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                return rs.getInt("id");
            }
            else
            {
                return -1;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    // Hole alle Karten aus dem Paket
    private List<String> getCardsFromPackage(int packageId)
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
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return cardIds;
    }

    // Löschen des Pakets
    private boolean deletePackage(int packageId)
    {
        // Löschen Sie alle Karten aus der package_cards Tabelle, die zum Paket gehören
        String deleteCardsQuery = "DELETE FROM package_cards WHERE package_id = ?";
        try (PreparedStatement deleteCardsStmt = connection.prepareStatement(deleteCardsQuery))
        {
            deleteCardsStmt.setInt(1, packageId);
            deleteCardsStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }

        // Löschen Sie das Paket aus der packages Tabelle
        String deletePackageQuery = "DELETE FROM packages WHERE id = ?";

        try (PreparedStatement deletePackageStmt = connection.prepareStatement(deletePackageQuery))
        {
            deletePackageStmt.setInt(1, packageId);
            deletePackageStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}