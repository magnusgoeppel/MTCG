package org.mtcg.app.controllers;

import org.mtcg.app.models.Card;
import org.mtcg.app.services.CardsService;
import org.mtcg.app.services.authService;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;
import org.mtcg.http.ContentType;
import java.util.List;

public class CardsController
{
    // Instanzen der Services
    private CardsService cardsService;
    private authService authService;

    public CardsController()
    {
        this.cardsService = new CardsService();
        this.authService = new authService();
    }

    // Abrufen der Karten eines Benutzers
    public Response handleGetCards(Request request)
    {
        // Extrahieren der userId aus dem Token
        int userId = authService.extractUserIdFromAuthHeader(request);

        if (userId == -1)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        try
        {
            // Speichern der Karten des Benutzers
            List<Card> cards = cardsService.getCardsForUser(userId);

            // Konvertieren der Karten in JSON
            String cardsJson = cardsService.convertToJson(cards);

            // Gebe züruck, ob die Karten leer sind oder erfolgreich abgerufen wurden
            if (cards.isEmpty())
            {
                return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, cardsJson);
            }
            return new Response(HttpStatus.OK, ContentType.JSON, cardsJson);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Internal server error");
        }
    }

    // Abrufen des Decks eines Benutzers
    public Response handleGetDeck(Request request)
    {
        // Extrahiere das Ausgabeformat aus dem Request
        String format = request.getQueryParams().get("format");

        // Extrahieren der userId aus dem Token
        int userId;

        try
        {
            userId = authService.extractUserIdFromAuthHeader(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Speichern des Decks des Benutzers
        List<Card>deck = cardsService.getDeckForUser(userId);

        // Überprüfe, ob das Deck in Plain oder JSON konvertiert werden soll
        String response;
        ContentType responseType;

        if("plain".equals(format))
        {
            responseType = ContentType.TEXT;
            response = cardsService.convertDeckToPlain(deck);
        }
        else
        {
            response = cardsService.convertToJson(deck);
            responseType = ContentType.JSON;
        }

        // Überprüfe, ob das Deck leer ist oder erfolgreich abgerufen wurde
        if (deck.isEmpty())
        {
            return new Response(HttpStatus.NO_CONTENT, responseType, response);
        }
        return new Response(HttpStatus.OK, responseType, response);
    }

    public Response handleConfigureDeck(Request request)
    {
        // Extrahieren der userId aus dem Token
        int userId;

        try
        {
            userId = authService.extractUserIdFromAuthHeader(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        try
        {
            // Überprüfe, ob die Anzahl der Karten im Deck gültig ist
            boolean isDeckSizeValid = cardsService.isDeckSizeValid(request.getBody());

            if (!isDeckSizeValid)
            {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "The deck must contain exactly 4 cards");
            }

            // Extrahieren der Karten-IDs aus dem Request Body
            List<String> cardIds = cardsService.extractCardIdsFromRequestBody(request.getBody());

            // Überprüfen Sie, ob alle Karten dem Benutzer gehören und verfügbar sind
            boolean areCardsValid = cardsService.areCardsValidAndBelongToUser(userId, cardIds);

            // Wenn die Karten ungültig sind, wird eine 403-Antwort zurückgegeben
            if (!areCardsValid)
            {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "The provided deck contains invalid cards");
            }

            // Überprüfen, ob das Deck erfolgreich konfiguriert wurde
            boolean success = cardsService.configureDeckForUser(userId, cardIds);

            if (success)
            {
                return new Response(HttpStatus.OK, ContentType.JSON, "The deck has been successfully configured");
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Internal server error");
    }
}
