package org.mtcg.database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup
{
    public static void createTables()
    {
        // Verbindung zur Datenbank aufbauen
        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement())
        {
            // Deck table
            String createDeckTable = "CREATE TABLE IF NOT EXISTS decks (" +
                                     "id SERIAL PRIMARY KEY" +
                                     ")";
            stmt.execute(createDeckTable);

            // User table
            String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                                     "id SERIAL PRIMARY KEY," +
                                     "username VARCHAR(255) UNIQUE NOT NULL," +
                                     "password VARCHAR(255) NOT NULL," +
                                     "coins INT DEFAULT 0," +
                                     "eloValue INT DEFAULT 0," +
                                     "token VARCHAR(255)," +
                                     "deck_id INT REFERENCES decks(id)" +
                                     ")";
            stmt.execute(createUserTable);

            // Card table
            String createCardTable = "CREATE TABLE IF NOT EXISTS cards (" +
                                     "id VARCHAR(255) PRIMARY KEY ," +
                                     "name VARCHAR(255) NOT NULL," +
                                     "damage DOUBLE PRECISION NOT NULL," +
                                     "elementType VARCHAR(255)," +
                                     "type VARCHAR(50)" +
                                     ")";
            stmt.execute(createCardTable);

            // User_Card relation table
            String createUserCardRelationTable = "CREATE TABLE IF NOT EXISTS user_cards (" +
                                                 "user_id INT REFERENCES users(id)," +
                                                 "card_id VARCHAR(255) REFERENCES cards(id)," +
                                                 "PRIMARY KEY (user_id, card_id)" +
                                                 ")";
            stmt.execute(createUserCardRelationTable);

            // Deck_Card relation table
            String createDeckCardRelationTable = "CREATE TABLE IF NOT EXISTS deck_cards (" +
                                                 "deck_id INT REFERENCES decks(id)," +
                                                 "card_id VARCHAR(255) REFERENCES cards(id)," +
                                                 "PRIMARY KEY (deck_id, card_id)" +
                                                 ")";
            stmt.execute(createDeckCardRelationTable);

            // Package table
            String createPackageTable = "CREATE TABLE IF NOT EXISTS packages (" +
                                        "id SERIAL PRIMARY KEY" +
                                        ")";
            stmt.execute(createPackageTable);

            // Package_Card relation table
            String createPackageCardRelationTable = "CREATE TABLE IF NOT EXISTS package_cards (" +
                                                    "package_id INT REFERENCES packages(id)," +
                                                    "card_id VARCHAR(255) REFERENCES cards(id)," +
                                                    "PRIMARY KEY (package_id, card_id)" +
                                                    ")";
            stmt.execute(createPackageCardRelationTable);

            // Battle table
            String createBattleTable = "CREATE TABLE IF NOT EXISTS battles (" +
                                       "id SERIAL PRIMARY KEY," +
                                       "user1_id INT REFERENCES users(id)," +
                                       "user2_id INT REFERENCES users(id)," +
                                       "log TEXT" +
                                       ")";
            stmt.execute(createBattleTable);

            // Trade table
            String createTradeTable = "CREATE TABLE IF NOT EXISTS trades (" +
                                      "id SERIAL PRIMARY KEY," +
                                      "offered_card_id VARCHAR(255) REFERENCES cards(id)," +
                                      "required_card_id VARCHAR(255) REFERENCES cards(id)," +
                                      "user_id INT REFERENCES users(id)" +
                                      ")";
            stmt.execute(createTradeTable);

            // Scoreboard table
            String createScoreboardTable = "CREATE TABLE IF NOT EXISTS scoreboards (" +
                                           "id SERIAL PRIMARY KEY," +
                                           "user_id INT REFERENCES users(id)," +
                                           "score INT" +
                                           ")";
            stmt.execute(createScoreboardTable);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
