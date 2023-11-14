package org.mtcg.app.controllers;

import org.mtcg.app.models.Card;
import org.mtcg.app.services.PackageService;
import org.mtcg.database.DatabaseConnection;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PackageController
{
    private PackageService packageService;
    private Connection connection;

    public PackageController()
    {
        this.packageService = new PackageService();
        this.connection = DatabaseConnection.getConnection();
    }

    public Response handleCreatePackage(Request request)
    {
        // Überprüfen Sie, ob der Benutzer "admin" ist und ob der Token gültig ist
        String authHeader = request.getHeaders().get("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer "))
        {
            String token = authHeader.substring(7);
            if ("admin-mtcgToken".equals(token)) {
                // Extrahieren Sie die Daten aus dem Anfragekörper
                String requestBody = request.getBody();
                // Konvertieren Sie den JSON-String in eine Liste von Kartenobjekten
                List<Card> cards = packageService.convertJsonToCards(requestBody);
                System.out.println("Cards: "+cards);

                // Erstellen Sie das Paket mit den Karten
                boolean isPackageCreated = packageService.createPackage(cards);

                if (isPackageCreated)
                {
                    return new Response(HttpStatus.CREATED, ContentType.JSON, "Package and cards successfully created");
                } else {
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "An error occurred while creating the package");
                }
            } else {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "Provided user is not \"admin\"");
            }
        } else {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized");
        }
    }
}

