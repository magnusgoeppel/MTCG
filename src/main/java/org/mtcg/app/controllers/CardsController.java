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
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid token");
        }

        try
        {
            List<Card> cards = cardsService.getCardsForUser(userId);
            String cardsJson = cardsService.convertCardsToJson(cards);
            return new Response(HttpStatus.OK, ContentType.JSON, cardsJson);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Internal server error");
        }
    }
}
