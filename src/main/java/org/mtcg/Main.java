package org.mtcg;

import org.mtcg.app.App;
import org.mtcg.database.DatabaseSetup;
import org.mtcg.server.Server;

public class Main
{
    public static void main(String[] args)
    {
        // Erstellt die Tabellen in der Datenbank (wenn sie noch nicht existieren)
        DatabaseSetup.createTables();

        // Startet den Server
        Server server = new Server();
        server.start();
    }
}
