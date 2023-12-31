package org.mtcg.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{
    // Datenbank URL, User und Passwort
    private static String DB_URL = "jdbc:postgresql://localhost:5432/mtcg";
    private static String USER = "admin";
    private static String PASS = "1234";

    // Methode zum Aufbau der Verbindung zur Datenbank
    public static Connection getConnection()
    {
        // Erstellt das Connection Objekt
        Connection connection = null;

        // Verbindung zur Datenbank aufbauen
        try
        {
            // Lädt den Treiber
            Class.forName("org.postgresql.Driver");
            // Stellt die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(DB_URL, USER, PASS);

        }
        catch (SQLException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        // Gibt die Verbindung zurück
        return connection;
    }
}
