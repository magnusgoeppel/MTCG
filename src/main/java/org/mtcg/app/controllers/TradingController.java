package org.mtcg.app.controllers;

import org.mtcg.app.models.TradeOffer;
import org.mtcg.app.services.CommonService;
import org.mtcg.app.services.TradingServices;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;

import java.util.List;

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

        // Extrahieren Sie die Trade-ID aus dem URL-Pfad
        String path = request.getPath();
        String[] pathParts = path.split("/");
        String tradeId = pathParts[pathParts.length - 1];

        // Überprüfen Sie, ob der Benutzer berechtigt ist, das Handelsangebot zu löschen
        boolean isOwner = tradingService.checkOwnership(userId, tradeId);

        if (!isOwner)
        {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "Forbidden: You do not own this trade");
        }

        // Versuchen Sie, das Handelsangebot zu löschen
        boolean success = tradingService.deleteTrade(tradeId);
        if (success)
        {
            return new Response(HttpStatus.OK, ContentType.JSON, "Trading deal successfully deleted");
        } else {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Internal server error: Unable to delete trade");
        }

    }

}
