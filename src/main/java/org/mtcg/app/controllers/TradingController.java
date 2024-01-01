package org.mtcg.app.controllers;

import org.mtcg.app.models.Card;
import org.mtcg.app.models.TradeOffer;
import org.mtcg.app.services.authService;
import org.mtcg.app.services.TradingService;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;
import java.util.List;

public class TradingController
{
    // Instanzen der Services
    private TradingService tradingService;

    private authService authService;

    public TradingController()
    {
        this.tradingService = new TradingService();
        this.authService = new authService();
    }

    // Abrufen der Handelsangebote
    public Response handleGetTradingDeals(Request request)
    {
        // Überprüfe, ob der Token gültig ist
        try
        {
            authService.extractUserIdFromAuthHeader(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Abrufen der Handelsangebote für den Benutzer
        List<TradeOffer> trades = tradingService.getTrades();

        // Überprüfen, ob Handelsangebote vorhanden sind
        if (trades.isEmpty())
        {
            return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, "No trades available");
        }
        else
        {
            // Konvertieren Sie die Handelsangebote in einen JSON-String
            String tradesJson = tradingService.convertTradesToJson(trades);
            return new Response(HttpStatus.OK, ContentType.JSON, tradesJson);
        }
    }

    // Erstellen eines Handelsangebots
    public Response handleCreateTradingDeal(Request request)
    {

        // Extrahieren die userId aus dem Token
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

        // Extrahieren der Daten aus dem Anfragekörper
        String requestBody = request.getBody();

        // Konvertieren den JSON-String in ein TradeOffer-Objekt
        TradeOffer trade = tradingService.convertJsonToTrade(requestBody);

        // Karte des Handelsangebots aus der DB holen
        Card tradeCard = tradingService.getCard(trade.getId());

        // Überprüfe, ob die Karte dem User gehört
        boolean isOwner = tradingService.checkOwnership(userId, trade.getId());

        // Überprüfe, ob die Karte im Deck gelockt ist
        boolean isCardLockedinDeck = tradingService.checkCardLockedinDeck(tradeCard, userId);

        if (!isOwner)
        {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "User does not own the card");
        }
        else if (isCardLockedinDeck)
        {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "Card is locked in deck");
        }

        // Überprüfen, ob das Handelsangebot bereits existiert
        boolean tradeExists = tradingService.checkTradeExists(trade.getId());

        if (tradeExists)
        {
            return new Response(HttpStatus.CONFLICT, ContentType.JSON, "A deal with this deal ID already exists");
        }

        // Erstellen Sie das Handelsangebot
        boolean isTradeCreated = tradingService.createTrade(trade, userId);

        // Gebe zurück, ob das Handelsangebot erfolgreich erstellt wurde
        if (isTradeCreated)
        {
            return new Response(HttpStatus.CREATED, ContentType.JSON, "Trade successfully created");
        }
        return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "An error occurred while creating the trade");
    }

    // Löschen des Handelsangebots
    public Response handleDeleteTradingDeal(Request request)
    {

        // Extrahieren die userId aus dem Token
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

        // Extrahieren der Trade-ID aus dem URL-Pfad
        String request_path = request.getPath();
        String tradeId = tradingService.extractTradeIdFromPath(request_path);

        // Überprüfen, ob der Benutzer das Handelsangebot besitzt
        boolean isOwner = tradingService.checkOwnership(userId, tradeId);

        if (!isOwner)
        {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "You do not own this trade");
        }

        // Überprüfen, ob das Handelsangebot bereits existiert
        boolean tradeExists = tradingService.checkTradeExists(tradeId);

        if (tradeExists)
        {
            return new Response(HttpStatus.CONFLICT, ContentType.JSON, "A deal with this deal ID already exists");
        }

        // Löschen des Handelsangebots
        int success = tradingService.deleteTrade(tradeId);

        if (success > 0)
        {
            return new Response(HttpStatus.OK, ContentType.JSON, "Trading deal successfully deleted");
        }
        else if (success == 0)
        {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "The provided deal ID was not found");
        }
        else
        {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "An error occurred while deleting the trade");
        }
    }

    public Response handleExecuteTrade(Request request)
    {
        // Extrahieren die userId aus dem Token
        int userId = authService.extractUserIdFromAuthHeader(request);

        if (userId == -1)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Extrahieren der Trade-ID aus dem URL-Pfad
        String request_path = request.getPath();
        // Extrahieren Sie die Trade-ID aus dem URL-Pfad
        String tradeId = tradingService.extractTradeIdFromPath(request_path);

        // Überprüfen, ob das Handelsangebot existiert
        boolean tradeExists = tradingService.checkTradeExists(tradeId);

        if(!tradeExists)
        {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "The provided deal ID was not found");
        }

        // Hole das Handelsangebot
        TradeOffer trade = tradingService.getTrade(tradeId);

        // Hole die Karte des Handelsangebots aud dem request body
        String requestBody = request.getBody();
        // Entfernen Sie die Anführungszeichen aus dem String
        String cardId = requestBody.replace("\"", "");

        // Hole die angebotene Karte aus der DB
        Card offeredCard = tradingService.getCard(cardId);

        // Überprüfen, ob der Benutzer das Handelsangebot besitzt
        boolean isOwner = tradingService.checkOwnership(userId, tradeId);
        // Überprüfen, ob die Karte dem Typ des Handelsangebots entspricht
        // und ob diese den minimalen Schadenswert hat
        boolean isCardTypeValid = tradingService.checkCardType(offeredCard, trade);
        // Überprüfen, ob die Karte im Deck gelockt ist
        boolean isCardLockedinDeck = tradingService.checkCardLockedinDeck(offeredCard, userId);


        if (isOwner)
        {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "Trade with yourself is not allowed");
        }
        else if(!isCardTypeValid)
        {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "Card type does not match the trade requirements");
        }
        else if(isCardLockedinDeck)
        {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "Card is locked in deck");
        }

        // Führen Sie den Handel aus
        boolean isTradeExecuted = tradingService.executeTrade(trade, offeredCard, userId);

        // Gebe zurück, ob der Handel erfolgreich ausgeführt wurde
        if (isTradeExecuted)
        {
            return new Response(HttpStatus.OK, ContentType.JSON, "Trade successfully executed");
        }
        else
        {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "An error occurred while executing the trade");
        }
    }
}
