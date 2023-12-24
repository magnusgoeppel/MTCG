package org.mtcg.app.controllers;

import org.mtcg.app.models.Card;
import org.mtcg.app.services.PackageService;
import org.mtcg.app.services.CommonService;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;


import java.util.List;

public class PackageController
{
    private PackageService packageService;
    private CommonService commonService;


    public PackageController()
    {
        this.packageService = new PackageService();
        this.commonService = new CommonService();

    }

    public Response handleCreatePackage(Request request)
    {
        // Überprüfen Sie, ob der Benutzer "admin" ist und ob der Token gültig ist
        String authHeader = request.getHeaders().get("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer "))
        {
            String token = authHeader.substring(7);

            if ("admin-mtcgToken".equals(token))
            {
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
                }
                else
                {
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "An error occurred while creating the package");
                }
            }
            else
            {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "Provided user is not \"admin\"");
            }
        }
        else
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized");
        }
    }
    public Response handleAcquirePackage(Request request)
    {
        // Extrahieren des Authorization-Headers
        String authHeader = request.getHeaders().get("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer "))
        {
            String token = authHeader.substring(7);


            // Überprüfen Sie den Token und holen Sie die Benutzer-ID
            int userId = commonService.getUserIdFromToken(token);

            if (userId == -1)
            {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid token");
            }

            // Versuchen Sie, das Paket zu erwerben
            boolean isPackageAcquired = packageService.acquirePackage(userId);

            if (isPackageAcquired)
            {
                return new Response(HttpStatus.OK, ContentType.JSON, "Package successfully acquired");
            }
            else
            {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "An error occurred while acquiring the package");
            }
        }
        else
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized");
        }
    }

}

