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

            String cardsJson = commonService.convertCardsToJson(cards);

            if (cards.isEmpty())
            {
                return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, cardsJson);
            }
            else
            {
                return new Response(HttpStatus.OK, ContentType.JSON, cardsJson);
            }
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

        try
        {
            List<Card>deck = cardsService.getDeckForUser(userId);

            // Wenn der Benutzer kein Deck hat, wird ein leeres JSON-Array zurückgegeben
            if (deck.isEmpty())
            {
                return new Response(HttpStatus.OK, ContentType.JSON, "[]");
            }

            String deckJson = commonService.convertCardsToJson(deck);

            return new Response(HttpStatus.OK, ContentType.JSON, deckJson);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Internal server error");
        }
    }

    public Response handleConfigureDeck(Request request)
    {
        String authHeader = request.getHeaders().get("Authorization");

        int userId;

        try
        {
            userId = commonService.extractUserIdFromAuthHeader(authHeader);
        } catch (Exception e)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        try
        {
            // Extrahieren der Karten-IDs aus dem Request Body
            List<String> cardIds = cardsService.extractCardIdsFromRequestBody(request.getBody());

            // Überprüfen Sie, ob alle Karten dem Benutzer gehören und verfügbar sind
            boolean areCardsValid = cardsService.areCardsValidAndBelongToUser(userId, cardIds);

            if (!areCardsValid)
            {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "The provided deck contains invalid cards");
            }

            boolean success = cardsService.configureDeckForUser(userId, cardIds);

            if (success)
            {
                return new Response(HttpStatus.OK, ContentType.JSON, "The deck has been successfully configured");
            }
            else
            {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "The provided deck did not include the required amount of cards or contains invalid cards");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Internal server error");
        }
    }
}
