package org.mtcg.app.controllers;

import org.mtcg.app.services.CommonService;
import org.mtcg.app.services.GameService;
import org.mtcg.app.services.PackageService;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;

public class GameController
{
    private GameService gameService;
    private CommonService commonService;


    public GameController()
    {
        this.gameService = new GameService();
        this.commonService = new CommonService();

    }

    public Response handleGetStats(Request request)
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

        String stats = gameService.getStats(userId);

        if (stats != null)
        {
            return new Response(HttpStatus.OK, ContentType.JSON, stats);
        }
        else
        {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "An error occurred while getting the stats");
        }
    }
}
