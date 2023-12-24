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
            e.printStackTrace();
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        try
        {
            List<Card> cards = cardsService.getCardsForUser(userId);
            String cardsJson = commonService.convertToJson(cards);
            return new Response(HttpStatus.OK, ContentType.JSON, cardsJson);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Internal server error");
        }
    }
}
