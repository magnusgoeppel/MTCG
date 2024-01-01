package org.mtcg.server;

import org.mtcg.app.controllers.PackageController;
import org.mtcg.app.controllers.UserController;
import org.mtcg.app.controllers.CardsController;
import org.mtcg.app.controllers.GameController;
import org.mtcg.app.controllers.TradingController;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.http.Method;

public class Router
{
    // Variablen für die Router-Klasse
    private final UserController userController;
    private final PackageController packageController;
    private final CardsController cardsController;
    private final GameController gameController;
    private final TradingController tradingController;


    // Konstruktor für die Router-Klasse
    public Router()
    {
        this.userController = new UserController();
        this.packageController = new PackageController();
        this.cardsController = new CardsController();
        this.gameController = new GameController();
        this.tradingController = new TradingController();

    }

    public Response route(Request request)
    {
        // Startseite
        if (request.getMethod() == Method.GET && "/".equals(request.getPath()))
        {
            String welcomeMessage = "Welcome to the Monster Card Trading Game Server!";

            return new Response(HttpStatus.OK, ContentType.HTML, welcomeMessage);
        }
        // Registrieren eines Benutzers
        if (request.getMethod() == Method.POST && "/users".equals(request.getPath()))
        {
            return userController.handleRegister(request);
        }
        // Einloggen eines Benutzers
        if (request.getMethod() == Method.POST && "/sessions".equals(request.getPath()))
        {
            return userController.handleLogin(request);
        }
        // Erstellen eines Pakets
        if (request.getMethod() == Method.POST && "/packages".equals(request.getPath()))
        {
            return packageController.handleCreatePackage(request);
        }
        // Erwerben eines Pakets
        if (request.getMethod() == Method.POST && "/transactions/packages".equals(request.getPath()))
        {
            return packageController.handleAcquirePackage(request);
        }
        // Abrufen der Karten eines Benutzers
        if (request.getMethod() == Method.GET && "/cards".equals(request.getPath()))
        {
            return cardsController.handleGetCards(request);
        }
        // Abrufen des Decks eines Benutzers
        if (request.getMethod() == Method.GET && "/deck".equals(request.getPath()))
        {
            return cardsController.handleGetDeck(request);
        }
        // Anpassen des Decks eines Benutzers
        if (request.getMethod() == Method.PUT && "/deck".equals(request.getPath()))
        {
            return cardsController.handleConfigureDeck(request);
        }
        // Abrufen von Benutzerdaten
        if(request.getMethod() == Method.GET && request.getPath().startsWith("/users/"))
        {
            return userController.handleGetUser(request);
        }
        // Aktualisieren von Benutzerdaten
        if (request.getMethod() == Method.PUT && request.getPath().startsWith("/users/"))
        {
            return userController.handleUpdateUser(request);
        }
        // Abrufen der Statistiken eines Benutzers
        if (request.getMethod() == Method.GET && "/stats".equals(request.getPath()))
        {
            return gameController.handleGetStats(request);
        }
        // Abrufen des Scoreboards
        if (request.getMethod() == Method.GET && "/scoreboard".equals(request.getPath()))
        {
            return gameController.handleGetScoreboard(request);
        }
        // Kampf zwischen zwei Benutzern ausführen und Ergebnis zurückgeben
        if (request.getMethod() == Method.POST && "/battles".equals(request.getPath()))
        {
            return gameController.handleBattle(request);
        }
        // Abrufen der Handelsangebote für den Benutzer
        if (request.getMethod() == Method.GET && "/tradings".equals(request.getPath()))
        {
            return tradingController.handleGetTradingDeals(request);
        }
        // Erstellen eines Handelsangebots
        if (request.getMethod() == Method.POST && "/tradings".equals(request.getPath()))
        {
            return tradingController.handleCreateTradingDeal(request);
        }
        // Löschen eines Handelsangebots
        if (request.getMethod() == Method.DELETE && request.getPath().startsWith("/tradings/"))
        {
            return tradingController.handleDeleteTradingDeal(request);
        }
        // Ausführen eines Handels
        if (request.getMethod() == Method.POST && request.getPath().startsWith("/tradings/"))
        {
            return tradingController.handleExecuteTrade(request);
        }
        // Wenn keine Route gefunden wurde, geben Sie eine Fehlermeldung zurück
        return new Response(HttpStatus.NOT_FOUND, ContentType.HTML, "Not Found");
    }
}