package org.mtcg.app.controllers;

import org.mtcg.app.models.Stats;
import org.mtcg.app.services.CommonService;
import org.mtcg.app.services.GameService;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;

import java.util.List;

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

    public Response handleGetScoreboard(Request request)
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

        try {
            List<Stats> scoreboard = gameService.getScoreboard();
            String scoreboardJson = gameService.convertScoreboardToJson(scoreboard);
            return new Response(HttpStatus.OK, ContentType.JSON, scoreboardJson);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Unable to retrieve scoreboard");
        }
    }

    public Response handleBattle(Request request)
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

        int opponentId = gameService.getOpponent(userId);


        if(opponentId > 0)
        {
            String battleLog = gameService.Battle(userId, opponentId);

            return new Response(HttpStatus.OK, ContentType.TEXT, battleLog);
        }
        if(opponentId == 0)
        {
            return new Response(HttpStatus.OK, ContentType.TEXT, "User cant battle himself");
        }
        else
        {
            // Füge den Opponent zur Datenbank hinzu
            gameService.addOpponent(userId);
            return new Response(HttpStatus.OK, ContentType.TEXT, "Waiting for opponent");
        }
    }
}
