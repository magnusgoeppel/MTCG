package org.mtcg;

import org.mtcg.database.DatabaseSetup;
import org.mtcg.server.Server;

public class Main
{
    public static void main(String[] args)
    {
        DatabaseSetup.createTables();

        Server server = new Server();
        server.start();
    }
}
