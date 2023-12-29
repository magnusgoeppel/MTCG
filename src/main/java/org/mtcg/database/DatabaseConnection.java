package org.mtcg.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{
    // URL, User und Passwort für die Datenbank
    private static String DB_URL = "jdbc:postgresql://localhost:5432/mtcg";
    private static String USER = "admin";
    private static String PASS = "1234";

    // Methode zum Aufbau der Verbindung zur Datenbank
    public static Connection getConnection()
    {
        Connection connection = null;

        // Verbindung zur Datenbank aufbauen
        try
        {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);

        }
        catch (SQLException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return connection;
    }
}
