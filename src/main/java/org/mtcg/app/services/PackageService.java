package org.mtcg.app.services;

import org.mtcg.app.models.Card;
import org.mtcg.database.DatabaseConnection;

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
                    String insertCardSql = "INSERT INTO cards (id, name, damage, elementType, type) VALUES (?, ?, ?, ?, ?) RETURNING id";

                    try (PreparedStatement insertCardStmt = connection.prepareStatement(insertCardSql))
                    {
                        insertCardStmt.setString(1, card.getId());
                        System.out.println(card.getId());
                        insertCardStmt.setString(2, card.getName());
                        insertCardStmt.setDouble(3, card.getDamage());
                        insertCardStmt.setString(4, card.getElementType());
                        insertCardStmt.setString(5, card.getType());
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

    public List<Card> convertJsonToCards(String json)
    {
        List<Card> cards = new ArrayList<>();
        // Trennt die einzelnen Karten
        String[] jsonCards = json.split("\\},\\{");

        for (String entry : jsonCards)
        {
            // Entfernt die umgebenden Klammern
            entry = entry.replace("[{", "").replace("}]", "");
            // Trennt die Eigenschaften der Karte
            String[] properties = entry.split(",");

            // Standardwerte, falls nicht vorhanden
            String id = "";
            String name = "";
            double damage = 0.0;
            String elementType = "";
            String type = "";

            for (String property : properties)
            {
                String[] keyValue = property.split("\":\"");
                if (keyValue.length == 2)
                {
                    String key = keyValue[0].replace("\"", "").trim();
                    String value = keyValue[1].replace("\"", "").trim();


                    switch (key)
                    {
                        case "Id":
                            id = value; // Behandeln Sie die ID als String
                            break;
                        case "Name":
                            name = value;
                            break;
                        case "Damage":
                            damage = Double.parseDouble(value); // Schäden sind Double-Werte
                            break;
                        case "Type":
                            type = value;
                            break;
                    }
                }
            }
            cards.add(new Card(id, name, damage, elementType, type));
        }
        return cards;
    }
}
