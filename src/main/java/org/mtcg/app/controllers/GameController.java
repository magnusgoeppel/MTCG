package org.mtcg.app.controllers;

import org.mtcg.app.models.Stats;
import org.mtcg.app.services.authService;
import org.mtcg.app.services.GameService;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;
import java.util.List;

public class GameController
{
    // Instanzen der Services
    private final GameService gameService;
    private final authService authService;


    public GameController()
    {
        this.gameService = new GameService();
        this.authService = new authService();

    }

    // Abrufen der Stats
    public Response handleGetStats(Request request)
    {
        // Extrahieren der userId aus dem Token
        int userId = authService.extractUserIdFromAuthHeader(request);

        if (userId == -1)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Hole die Stats des Benutzers
        String stats = gameService.getStats(userId);

        // Überprüfe, ob die Stats erfolgreich abgerufen werden konnten
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
        // Überprüfe, ob der Token gültig ist
        int userId = authService.extractUserIdFromAuthHeader(request);

        if (userId == -1)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        try
        {
            // Hole das Scoreboard
            List<Stats> scoreboard = gameService.getScoreboard();
            // Konvertiere das Scoreboard in ein JSON-Objekt
            String scoreboardJson = gameService.convertScoreboardToJson(scoreboard);
            // Gebe das Scoreboard zurück
            return new Response(HttpStatus.OK, ContentType.JSON, scoreboardJson);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Unable to retrieve scoreboard");
        }
    }

    // Ausführen des Kampfes
    public Response handleBattle(Request request)
    {
        // Extrahieren der userId aus dem Token
        int userId = authService.extractUserIdFromAuthHeader(request);

        if (userId == -1)
        {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Unauthorized: Invalid or missing token");
        }

        // Suche nach einem Opponent
        int opponentId = gameService.getOpponent(userId);

        // Wenn ein Opponent gefunden wurde, führe den Kampf aus
        if(opponentId > 0)
        {
            String battleLog = gameService.Battle(userId, opponentId);

            // Gebe den Kampflog zurück
            return new Response(HttpStatus.OK, ContentType.TEXT, battleLog);
        }
        // Wenn der Benutzer versucht gegen sich selbst zu kämpfen
        if(opponentId == 0)
        {
            return new Response(HttpStatus.OK, ContentType.TEXT, "User cant battle himself");
        }
        else
        {
            // Wenn kein Opponent gefunden wurde, füge den Benutzer der Queue hinzu
            gameService.addOpponent(userId);
            return new Response(HttpStatus.OK, ContentType.TEXT, "Waiting for opponent");
        }
    }
}
