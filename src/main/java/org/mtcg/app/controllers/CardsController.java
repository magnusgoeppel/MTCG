package org.mtcg.app.controllers;

import org.mtcg.app.models.Card;
import org.mtcg.app.services.CardsService;
import org.mtcg.app.services.CommonService;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;
import org.mtcg.http.ContentType;

import java.sql.SQLException;
import java.util.List;

public class CardsController
{
    private CardsService cardsService;
    private CommonService commonService;

    public CardsController()
    {
        this.cardsService = new CardsService();
        this.commonService = new CommonService();
    }

    public Response handleGetCards(Request request)
    {
        // Extrahieren des Authorization-Headers
        String authHeader = request.getHeaders().get("Authorization");

        int userId;
        try
        {
            userId = commonService.extractUserIdFromAuthHeader(authHeader);
        }
        catch (Exception e)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        try
        {
            List<Card> cards = cardsService.getCardsForUser(userId);

            String cardsJson = cardsService.convertToJson(cards);

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

    public Response handleGetDeck(Request request)
    {
        String authHeader = request.getHeaders().get("Authorization");
        String format = request.getQueryParams().get("format");

        int userId;
        try
        {
            userId = commonService.extractUserIdFromAuthHeader(authHeader);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        List<Card>deck = cardsService.getDeckForUser(userId);

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

        // Wenn das Deck leer ist, wird eine 204-Antwort zurückgegeben
        if (deck.isEmpty())
        {
            return new Response(HttpStatus.NO_CONTENT, responseType, response);
        }
        return new Response(HttpStatus.OK, responseType, response);
    }

    public Response handleConfigureDeck(Request request)
    {
        String authHeader = request.getHeaders().get("Authorization");

        int userId;

        try
        {
            userId = commonService.extractUserIdFromAuthHeader(authHeader);
        }
        catch (Exception e)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        try
        {
            // Überprüfe die Anzahl der Karten im Deck
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
