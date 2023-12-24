package org.mtcg.app.controllers;

import org.mtcg.app.services.DeckService;
import org.mtcg.app.services.CommonService;
import org.mtcg.app.models.Card;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;

import java.sql.SQLException;
import java.util.List;


public class DeckController
{
    private DeckService deckService;
    private CommonService commonService;

    public DeckController()
    {
        this.deckService = new DeckService();
        this.commonService = new CommonService();
    }

    public Response handleGetDeck(Request request)
    {
        String authHeader = request.getHeaders().get("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: No token provided");
        }

        String token = authHeader.substring(7);
        int userId;

        try
        {
            userId = commonService.getUserIdFromToken(token);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid token");
        }

        try
        {
            List<Card> deck = deckService.getDeckForUser(userId);
            String deckJson = commonService.convertToJson(deck);

            return new Response(HttpStatus.OK, ContentType.JSON, deckJson);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Internal server error");
        }
    }
}

