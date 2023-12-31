package org.mtcg.app.controllers;

import org.mtcg.app.models.Card;
import org.mtcg.app.services.PackageService;
import org.mtcg.app.services.CommonService;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;


import java.sql.SQLException;
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

            if (packageService.getAdminToken().equals(token))
            {
                // Extrahieren Sie die Daten aus dem Anfragekörper
                String requestBody = request.getBody();
                // Konvertieren Sie den JSON-String in eine Liste von Kartenobjekten
                List<Card> cards = packageService.convertJsonToCards(requestBody);
                System.out.println("Cards: "+cards);

                boolean isCardDouble = packageService.checkCardsExistence(cards);

                if(isCardDouble)
                {
                    return new Response(HttpStatus.CONFLICT, ContentType.JSON, "Card already exists");
                }
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
            else if (packageService.getAdminToken() == null)
            {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "An error occurred while creating the package");
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

        int userId;
        try
        {
            userId = commonService.extractUserIdFromAuthHeader(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Überprüfen Sie, ob der Benutzer genügend Münzen hat, um das Paket zu kaufen
        boolean hasEnoughCoins = packageService.checkCoins(userId);

        if (!hasEnoughCoins)
        {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "The user doesn't have enough coins to acquire the package");
        }

        // Überprüfen Sie, ob ein Paket zum Kauf verfügbar ist
        boolean isPackageAvailable = packageService.checkPackageAvailable();

        if(!isPackageAvailable)
        {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "No card package available for buying");
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
}


