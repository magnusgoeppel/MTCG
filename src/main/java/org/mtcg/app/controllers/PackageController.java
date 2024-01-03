package org.mtcg.app.controllers;

import lombok.Setter;
import org.mtcg.app.models.Card;
import org.mtcg.app.services.PackageService;
import org.mtcg.app.services.AuthService;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;
import java.util.List;

@Setter
public class PackageController
{
    private PackageService packageService;
    private AuthService authService;

    public PackageController()
    {
        this.packageService = new PackageService();
        this.authService = new AuthService();

    }

    // Erstellen eines Pakets
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
                // Konvertieren den JSON-String in eine Liste von Kartenobjekten
                List<Card> cards = packageService.convertJsonToCards(requestBody);

                // Überprüfen Sie, ob die Karten bereits existieren
                boolean isCardDouble = packageService.checkCardsExistence(cards);

                if(isCardDouble)
                {
                    return new Response(HttpStatus.CONFLICT, ContentType.JSON, "Card already exists");
                }

                // Erstellen des Pakets und der Karten
                boolean isPackageCreated = packageService.createPackage(cards);

                // Überprüfen, ob das Paket und die Karten erfolgreich erstellt wurden
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
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "Provided user is not admin");
            }
        }
        else
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized");
        }
    }

    // Paket erwerben
    public Response handleAcquirePackage(Request request)
    {
        // Extrahieren der userId aus dem Token
        int userId = authService.extractUserIdFromAuthHeader(request);

        if (userId == -1)
        {
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

        // Versuchen das Paket zu erwerben
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