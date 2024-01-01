package org.mtcg.app.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mtcg.app.models.Card;
import org.mtcg.app.models.Stats;
import org.mtcg.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameService
{
    // Verbindung zur Datenbank
    private Connection connection;

    public GameService()
    {
        this.connection = DatabaseConnection.getConnection();
    }

    // Hole die Stats des Benutzers
    public String getStats(int userId)
    {
        String query = "SELECT username FROM users WHERE id = ?";
        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                String username = rs.getString("username");

                // Hole die Stats aus der stats Tabelle
                query = "SELECT * FROM stats WHERE user_id = ?";
                stmt = connection.prepareStatement(query);
                stmt.setInt(1, userId);
                rs = stmt.executeQuery();

                if (rs.next())
                {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode stats = mapper.createObjectNode();

                    stats.put("Username", username);
                    stats.put("Elo", rs.getInt("elo"));
                    stats.put("Wins", rs.getInt("wins"));
                    stats.put("Losses", rs.getInt("losses"));

                    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(stats);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // Hole das Scoreboard
    public List<Stats> getScoreboard()
    {
        List<Stats> scoreboard = new ArrayList<>();

        try
        {
            String query = "SELECT u.username, s.* " +
                    "FROM users u " +
                    "JOIN stats s ON u.id = s.user_id " +
                    "JOIN scoreboards sc ON u.id = sc.user_id " +
                    "ORDER BY sc.place ASC";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                String username = rs.getString("username");
                int elo = rs.getInt("elo");
                int wins = rs.getInt("wins");
                int losses = rs.getInt("losses");

                scoreboard.add(new Stats(elo, wins, losses, username));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return scoreboard;
    }

    // Konvertiere das Scoreboard in ein JSON-Objekt
    public String convertScoreboardToJson(List<Stats> scoreboard)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            return mapper.writeValueAsString(scoreboard);
        } catch (Exception e)
        {
            e.printStackTrace();
            return "[]";
        }
    }

    // Suche nach einem Gegner
    public int getOpponent(int userId)
    {
        // SQL-Query, um einen wartenden Gegner zu finden
        String findOpponentQuery = "SELECT id, user2_id FROM battles WHERE user1_id IS NULL AND user2_id IS NOT NULL LIMIT 1";

       // SQL-Query, um den aktuellen Benutzer in den gefundenen Kampf einzufügen
        String updateBattleQuery = "UPDATE battles SET user1_id = ? WHERE id = ?";

        try
        {
            // Versuche, einen wartenden Gegner zu finden
            PreparedStatement findStmt = connection.prepareStatement(findOpponentQuery);
            ResultSet rs = findStmt.executeQuery();

            if (rs.next())
            {
                int battleId = rs.getInt("id");
                int opponentId = rs.getInt("user2_id");

                // Überprüfen Sie, ob der Benutzer versucht, gegen sich selbst zu kämpfen
                if(userId == opponentId)
                {
                    return 0;
                }

                // Füge den aktuellen Benutzer in den gefundenen Kampf ein
                PreparedStatement updateStmt = connection.prepareStatement(updateBattleQuery);
                updateStmt.setInt(1, userId);
                updateStmt.setInt(2, battleId);
                int affectedRows = updateStmt.executeUpdate();

                // Überprüfen Sie, ob das Update erfolgreich war
                if (affectedRows == 1)
                {
                    return opponentId;
                }
                else
                {
                    return -1;
                }
            }
            else
            {

                return -1;
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    // Führe einen Kampf zwischen zwei Benutzern aus
    public synchronized String Battle(int userId, int opponentId)
    {

        StringBuilder battleLog = new StringBuilder();
        String username = getUserName(userId);
        String opponentname = getUserName(opponentId);
        int battleId = getBattleId(userId, opponentId);
        boolean noSpecialCase = true;

        battleLog.append("Battle ").append(battleId).append(": ").append(username).append(" vs ").append(opponentname).append("\n");

        // Hole das Deck aus der DB
        List<String> userDeck = getDeck(userId);
        List<String> opponentDeck = getDeck(opponentId);

        for (int round = 1; round <= 100; ++round)
        {
            // Wähle radom eine Karte aus dem userDeck und dem opponentDeck
            String userCardId = getRandomCardId(userDeck);
            String opponentCardId = getRandomCardId(opponentDeck);

            // Erstellen Sie ein Card-Objekt für den User und den Gegner
            Card userCard = getCard(userCardId);
            Card opponentCard = getCard(opponentCardId);

            double userCardDamage = userCard.getDamage();
            double opponentCardDamage = opponentCard.getDamage();

            battleLog.append("Round ").append(round).append(": ");
            battleLog.append(username).append(": ").append(userCard.getName()).append(" (").append(userCardDamage).append(" Damage) vs. ");
            battleLog.append(opponentname).append(": ").append(opponentCard.getName()).append(" (").append(opponentCardDamage).append(" Damage)").append(" => ");

            // Führen Sie den Kampf aus
            // Monster vs Monster
            if (!userCard.getName().endsWith("Spell") && !opponentCard.getName().endsWith("Spell"))
            {
                // Goblins greifen keine Drachen an.
                if (userCard.getName().endsWith("Goblin") && opponentCard.getName().endsWith("Dragon"))
                {
                    battleLog.append("Goblin are too afraid of Dragons to attack => ");
                    battleLog.append(username).append(": ").append("Dragon wins").append("\n");

                    opponentDeck.add(userCardId);
                    userDeck.remove(userCardId);

                    noSpecialCase = false;
                }
                else if (userCard.getName().endsWith("Dragon") && opponentCard.getName().endsWith("Goblin"))
                {
                    battleLog.append("Goblin are too afraid of Dragons to attack => ");
                    battleLog.append(opponentname).append(": ").append("Dragon wins").append("\n");

                    userDeck.add(opponentCardId);
                    opponentDeck.remove(opponentCardId);

                    noSpecialCase = false;
                }
                // Wizzard kontrollieren Orks
                else if(userCard.getName().endsWith("Wizzard") && opponentCard.getName().endsWith("Ork"))
                {
                    battleLog.append("Wizzard can control Orks so they are not able to damage them => ");
                    battleLog.append(username).append(": ").append("Dragon wins").append("\n");

                    opponentDeck.add(userCardId);
                    userDeck.remove(userCardId);

                    noSpecialCase = false;
                }
                else if(userCard.getName().endsWith("Ork") && opponentCard.getName().endsWith("Wizzard"))
                {
                    battleLog.append("Wizzard can control Orks so they are not able to damage them => ");
                    battleLog.append(opponentname).append(": ").append("Dragon wins").append("\n");

                    userDeck.add(opponentCardId);
                    opponentDeck.remove(opponentCardId);

                    noSpecialCase = false;
                }
                // Feuerelfen können Drachenangriffen ausweichen
                else if (userCard.getName().equals("FireElf") && opponentCard.getName().endsWith("Dragon"))
                {
                    battleLog.append("FireElves can dodge Dragon attacks => ");
                    battleLog.append(username).append(": ").append("FireElves wins").append("\n");

                    opponentDeck.add(userCardId);
                    userDeck.remove(userCardId);

                    noSpecialCase = false;
                }
                else if (userCard.getName().endsWith("Dragon") && opponentCard.getName().equals("FireElf"))
                {
                    battleLog.append("FireElves can dodge Dragon attacks => ");
                    battleLog.append(opponentname).append(": ").append("FireElves wins").append("\n");

                    userDeck.add(opponentCardId);
                    opponentDeck.remove(opponentCardId);

                    noSpecialCase = false;
                }
            }
            // Spell vs Spell
            else if (userCard.getName().endsWith("Spell") && opponentCard.getName().endsWith("Spell"))
            {
                if ((userCard.getName().equals("WaterSpell") && opponentCard.getName().equals("FireSpell")) ||
                        (userCard.getName().equals("FireSpell") && opponentCard.getName().equals("RegularSpell")) ||
                        (userCard.getName().equals("RegularSpell") && opponentCard.getName().equals("WaterSpell")))
                {
                    userCardDamage *= 2;
                    opponentCardDamage /= 2;
                }
                else if ((userCard.getName().equals("FireSpell") && opponentCard.getName().equals("WaterSpell")) ||
                        (userCard.getName().equals("RegularSpell") && opponentCard.getName().equals("FireSpell")) ||
                        (userCard.getName().equals("WaterSpell") && opponentCard.getName().equals("RegularSpell")))
                {
                    userCardDamage /= 2;
                    opponentCardDamage *= 2;
                }
            }
            // Monster vs Spell
            else if (!userCard.getName().endsWith("Spell") && opponentCard.getName().endsWith("Spell"))
            {
                // Ritter ertrinken sofort gegen Wassersprüche
                if (userCard.getName().endsWith("Knight") && opponentCard.getName().equals("WaterSpell"))
                {
                    battleLog.append("The armor of Knights is so heavy that WaterSpells make them drown them instantly => ");
                    battleLog.append(opponentname).append(": ").append("WaterSpells wins").append("\n");

                    userDeck.add(opponentCardId);
                    opponentDeck.remove(opponentCardId);

                    noSpecialCase = false;
                }
                // Kraken sind immun gegen Zauber.
                else if (userCard.getName().endsWith("Kraken"))
                {
                    battleLog.append("Kraken are immune to spells => ");
                    battleLog.append(username).append(": ").append("Kraken wins").append("\n");

                    opponentDeck.add(userCardId);
                    userDeck.remove(userCardId);

                    noSpecialCase = false;
                }
                else if ((userCard.getName().startsWith("Water") && opponentCard.getName().equals("FireSpell")) ||
                        (!userCard.getName().startsWith("Water") && opponentCard.getName().equals("RegularSpell")) ||
                        ((!userCard.getName().startsWith("Water") && !userCard.getName().startsWith("Fire")) && opponentCard.getName().equals("WaterSpell")))
                {
                    userCardDamage *= 2;
                    opponentCardDamage /= 2;
                }
                else if ((userCard.getName().startsWith("Fire") && opponentCard.getName().equals("WaterSpell")) ||
                        ((!userCard.getName().startsWith("Water") && !userCard.getName().startsWith("Fire")) && opponentCard.getName().equals("FireSpell")) ||
                        (userCard.getName().startsWith("Water") && opponentCard.getName().equals("RegularSpell")))
                {
                    userCardDamage /= 2;
                    opponentCardDamage *= 2;
                }
            }
            // Spell vs Monster
            else if (userCard.getName().endsWith("Spell") && !opponentCard.getName().endsWith("Spell"))
            {
                // Ritter ertrinken sofort gegen Wassersprüche
                if (userCard.getName().equals("WaterSpell") && opponentCard.getName().endsWith("Knight"))
                {
                    battleLog.append("The armor of Knights is so heavy that WaterSpells make them drown them instantly => ");
                    battleLog.append(username).append(": ").append("WaterSpells wins").append("\n");

                    opponentDeck.add(userCardId);
                    userDeck.remove(userCardId);

                    noSpecialCase = false;
                }
                // Kraken sind immun gegen Zauber.
                else if (opponentCard.getName().endsWith("Kraken"))
                {
                    battleLog.append("Kraken are immune to spells ->");
                    battleLog.append(opponentname).append(": ").append("Kraken wins").append("\n");

                    userDeck.add(opponentCardId);
                    opponentDeck.remove(opponentCardId);

                    noSpecialCase = false;
                }

                else if ((userCard.getName().equals("WaterSpell") && opponentCard.getName().startsWith("Fire")) ||
                        (userCard.getName().equals("FireSpell") && (!opponentCard.getName().startsWith("Water") && !opponentCard.getName().startsWith("Fire"))) ||
                        (userCard.getName().equals("RegularSpell") && opponentCard.getName().startsWith("Water")))
                {
                    userCardDamage *= 2;
                    opponentCardDamage /= 2;

                }
                else if ((userCard.getName().equals("FireSpell") && opponentCard.getName().startsWith("Water")) ||
                        (userCard.getName().equals("RegularSpell") && opponentCard.getName().startsWith("Fire")) ||
                        (userCard.getName().equals("WaterSpell") && (!opponentCard.getName().startsWith("Water") && !opponentCard.getName().startsWith("Fire"))))
                {
                    userCardDamage /= 2;
                    opponentCardDamage *= 2;
                }
            }

            // Wenn kein Sonderfall eintritt, vergleichen Sie die Schadenswerte der Karten
            if(noSpecialCase)
            {
                battleLog.append(userCard.getDamage()).append(" vs ").append(opponentCard.getDamage()).append(" -> ");
                battleLog.append(userCardDamage).append(" vs ").append(opponentCardDamage).append(" => ");

                if (userCardDamage > opponentCardDamage)
                {
                    battleLog.append(username).append(": ").append(userCard.getName()).append(" wins").append("\n");
                    // Lösche die Karte aus dem Deck
                    userDeck.add(opponentCardId);
                    opponentDeck.remove(opponentCardId);
                }
                else if (userCardDamage < opponentCardDamage)
                {
                    battleLog.append(opponentname).append(": ").append(opponentCard.getName()).append(" wins").append("\n");
                    // Lösche die Karte aus dem Deck
                    opponentDeck.add(userCardId);
                    userDeck.remove(userCardId);
                }
                else
                {
                    battleLog.append("Draw").append("\n");
                }
            }
            noSpecialCase = true;

            // Überprüfen Sie, ob der User oder der Gegner gewonnen hat
            if (userDeck.isEmpty())
            {
                battleLog.append(username).append(" has no cards left. ").append(opponentname).append(" wins!");
                // Update stats
                updateStats(opponentId, userId);
                break;
            }
            else if (opponentDeck.isEmpty())
            {
                battleLog.append(opponentname).append(" has no cards left. ").append(username).append(" wins!");
                // Update stats
                updateStats(userId, opponentId);
                break;
            }
            else if(round == 100)
            {
                battleLog.append("No winner after 100 rounds. It's a draw!");
            }
        }
        // Speichern Sie das Battle-Log in der DB
        saveBattleLog(battleId, battleLog.toString());
        // Aktualisieren das Scoreboard
        updateScoreboard();

        // Geben Sie das Battle-Log zurück
        return battleLog.toString();
    }

    // Füge den Benutzer zur Queue hinzu
    public void addOpponent(int userId)
    {
        String query = "INSERT INTO battles (user2_id) VALUES (?)";
        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ------------------------------ Hilfsfunktionen ------------------------------

    private Card getCard(String cardId)
    {
        // Wählen Sie die Karte mit der übergebenen ID aus der cards Tabelle aus
        String query = "SELECT * FROM cards WHERE id = ?";
        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, cardId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                // Extrahieren Sie die Werte aus dem ResultSet
                String name = rs.getString("name");
                int damage = rs.getInt("damage");

                return new Card(cardId, name, damage);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private String getUserName(int userId)
    {
        // Wählen Sie den Namen des Users mit der übergebenen ID aus der users Tabelle aus
        String query = "SELECT username FROM users WHERE id = ?";
        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                return rs.getString("username");
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> getDeck(int userId)
    {
        List<String> deck = new ArrayList<>();
        // SQL-Abfrage, die einen JOIN zwischen den Tabellen 'users' und 'deck_cards' verwendet
        String query = "SELECT dc.card_id FROM deck_cards dc " +
                "JOIN users u ON u.deck_id = dc.deck_id " +
                "WHERE u.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery())
            {
                while (rs.next()) {
                    deck.add(rs.getString("card_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deck;
    }

    private String getRandomCardId(List<String> deck)
    {
        if (!deck.isEmpty())
        {
            // Wähle eine zufällige Karte aus dem Deck aus
            Random random = new Random();
            int idx = random.nextInt(deck.size());
            return deck.get(idx);
        }
        else
        {
            return null;
        }
    }

    // 3P für Sieg, -5P für Niederlage
    private void updateStats(int userId, int opponentId)
    {
        // Hole die Stats des Users aus der DB
        Stats userStats = getStatsFromDB(userId);
        Stats opponentStats = getStatsFromDB(opponentId);

        // 3P für Sieg, -5P für Niederlage

        userStats.setElo(userStats.getElo() + 3);
        userStats.setWins(userStats.getWins() + 1);
        opponentStats.setElo(opponentStats.getElo() - 5);
        opponentStats.setLosses(opponentStats.getLosses() + 1);

        if(userStats.getElo() < 0)
        {
            userStats.setElo(0);
        }
        if(opponentStats.getElo() < 0)
        {
            opponentStats.setElo(0);
        }

        // Speichern Sie die Stats in der DB
        saveStatsToDB(userId, userStats);
        saveStatsToDB(opponentId, opponentStats);
    }

    private Stats getStatsFromDB(int userId)
    {
        // Wählen Sie die Stats des Users mit der übergebenen ID aus der stats Tabelle aus
        String query = "SELECT u.username, s.* " +
                "FROM users u " +
                "JOIN stats s ON u.id = s.user_id " +
                "WHERE u.id = ?";
        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                // Extrahieren Sie die Werte aus dem ResultSet
                String username = rs.getString("username");
                int elo = rs.getInt("elo");
                int wins = rs.getInt("wins");
                int losses = rs.getInt("losses");

                return new Stats(elo, wins, losses, username);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private void saveStatsToDB(int userId, Stats userStats)
    {
        // Definieren Sie die SQL-Abfrage zum Aktualisieren der Statistiken
        String updateQuery = "UPDATE stats SET elo = ?, wins = ?, losses = ? WHERE user_id = ?";

        try {
            // Erstellen Sie ein PreparedStatement für die SQL-Abfrage
            PreparedStatement stmt = connection.prepareStatement(updateQuery);

            // Setzen Sie die Parameter für das PreparedStatement
            stmt.setInt(1, userStats.getElo());
            stmt.setInt(2, userStats.getWins());
            stmt.setInt(3, userStats.getLosses());
            stmt.setInt(4, userId);

            // Führen Sie das Update aus
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0)
            {
                throw new SQLException("Updating stats failed, no rows affected.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private int getBattleId(int userId, int opponentId)
    {
        // Wählen Sie die ID des Kampfes aus der battles Tabelle aus
        String query = "SELECT id FROM battles WHERE user1_id = ? AND user2_id = ? AND log IS NULL";
        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setInt(2, opponentId);
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
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    private void saveBattleLog(int battleId, String battleLog) {
        // SQL-Query, um das Battle-Log zu aktualisieren
        String query = "UPDATE battles SET log = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query))
        {


            stmt.setString(1, battleLog);
            stmt.setInt(2, battleId);

            // Ausführen des Updates
            int affectedRows = stmt.executeUpdate();

            // Überprüfen Sie, ob das Update erfolgreich war
            if (affectedRows == 0)
            {
                throw new SQLException("Updating the battle log failed, no rows affected.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void updateScoreboard() {
        try {
            // Beginnen Sie eine Transaktion
            connection.setAutoCommit(false);

            // Holen Sie sich die sortierte Liste der Benutzer nach ELO
            String getSortedUsersQuery = "SELECT user_id FROM stats ORDER BY elo DESC";
            PreparedStatement getSortedUsersStmt = connection.prepareStatement(getSortedUsersQuery);
            ResultSet sortedUsers = getSortedUsersStmt.executeQuery();

            // Aktualisieren Sie die Platzierungen für jeden Benutzer
            int place = 1; // Starten Sie die Platzierung bei 1
            while (sortedUsers.next())
            {
                int userId = sortedUsers.getInt("user_id");

                // Aktualisieren den Platz des Benutzers im Scoreboard
                String updatePlaceQuery = "UPDATE scoreboards SET place = ? WHERE user_id = ?";
                PreparedStatement updatePlaceStmt = connection.prepareStatement(updatePlaceQuery);
                updatePlaceStmt.setInt(1, place);
                updatePlaceStmt.setInt(2, userId);
                updatePlaceStmt.executeUpdate();

                ++place; // Gehen Sie zum nächsten Platz über
            }

            // Commit der Transaktion
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // Im Fehlerfall die Transaktion zurückrollen
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                // Stellen Sie sicher, dass AutoCommit wieder aktiviert ist
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

}
