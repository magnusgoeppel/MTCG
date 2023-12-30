package org.mtcg.app.controllers;

import org.mtcg.app.models.Card;
import org.mtcg.app.models.TradeOffer;
import org.mtcg.app.services.CommonService;
import org.mtcg.app.services.TradingServices;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class TradingController
{
    private TradingServices tradingService;

    private CommonService commonService;

    public TradingController()
    {
        this.tradingService = new TradingServices();
        this.commonService = new CommonService();
    }

    public Response handleGetTradingDeals(Request request)
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

        // Abrufen der Handelsangebote für den Benutzer
        List<TradeOffer> trades = tradingService.getTrades();

        // Überprüfen Sie, ob Handelsangebote vorhanden sind
        if (trades.isEmpty())
        {
            System.out.println("No trades available");
            return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, "No trades available");
        }
        else
        {
            String tradesJson = tradingService.convertTradesToJson(trades);
            return new Response(HttpStatus.OK, ContentType.JSON, tradesJson);
        }
    }

    public Response handleCreateTradingDeal(Request request)
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

        // Extrahieren Sie die Daten aus dem Anfragekörper
        String requestBody = request.getBody();
        // Konvertieren Sie den JSON-String in ein TradeOffer-Objekt
        TradeOffer trade = tradingService.convertJsonToTrade(requestBody);

        // Erstellen Sie das Handelsangebot
        boolean isTradeCreated = tradingService.createTrade(trade, userId);

        if (isTradeCreated)
        {
            return new Response(HttpStatus.CREATED, ContentType.JSON, "Trade successfully created");
        }
        else
        {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "An error occurred while creating the trade");
        }
    }

    public Response handleDeleteTradingDeal(Request request)
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

        String request_path = request.getPath();
        // Extrahieren Sie die Trade-ID aus dem URL-Pfad
        String tradeId = tradingService.extractTradeIdFromPath(request_path);

        // Überprüfen Sie, ob der Benutzer berechtigt ist, das Handelsangebot zu löschen
        boolean isOwner = tradingService.checkOwnership(userId, tradeId);

        if (!isOwner)
        {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "Forbidden: You do not own this trade");
        }

        // Versuchen Sie, das Handelsangebot zu löschen
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

        String request_path = request.getPath();
        // Extrahieren Sie die Trade-ID aus dem URL-Pfad
        String tradeId = tradingService.extractTradeIdFromPath(request_path);

        // TradeId in der DB?
        boolean tradeExists = tradingService.checkTradeExists(tradeId);

        if(!tradeExists)
        {
            return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "The provided deal ID was not found");
        }

        // Hole das Handelsangebot
        TradeOffer trade = tradingService.getTrade(tradeId);

        // Hole die Id der Karte die gehandelt werden soll aus dem Anfragekörper
        String requestBody = request.getBody();
        String cardId = tradingService.extractCardIdFromJson(requestBody);

        // Karte Objekt aus der DB holen
        Card offeredCard = tradingService.getCard(cardId);

        // 403: offeredCard gehört nicht dem User,
        boolean isOwner = tradingService.checkOwnership(userId, tradeId);
        // oder die Anforderungen werden nicht erfüllt (Typ, MinimumDamage),
        boolean isCardTypeValid = tradingService.checkCardType(offeredCard, trade);
        // oder die offeredCard ist im Deck gelockt
        boolean isCardLockedinDeck = tradingService.checkCardLockedinDeck(offeredCard, userId);

        // Trade mit sich selbst ist nicht erlaubt
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


        boolean isTradeExecuted = tradingService.executeTrade(trade, offeredCard, userId);

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
