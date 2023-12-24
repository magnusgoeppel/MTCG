package org.mtcg.server;

import org.mtcg.app.controllers.PackageController;
import org.mtcg.app.controllers.UserController;
import org.mtcg.app.controllers.CardsController;
import org.mtcg.http.ContentType;
import org.mtcg.http.HttpStatus;
import org.mtcg.http.Method;


public class Router
{
    // Variablen für die Router-Klasse
    private final UserController userController;
    private final PackageController packageController;
    private final CardsController cardsController;

    // Konstruktor für die Router-Klasse
    public Router()
    {
        this.userController = new UserController();
        this.packageController = new PackageController();
        this.cardsController = new CardsController();

    }

    // Methode zum Routen der HTTP-Requests
    public Response route(Request request)
    {

        if (request.getMethod() == Method.GET && "/".equals(request.getPath()))
        {
            String welcomeMessage = "Welcome to the Monster Card Trading Game Server!";

            return new Response(HttpStatus.OK, ContentType.HTML, welcomeMessage);
        }

        if (request.getMethod() == Method.POST && "/users".equals(request.getPath()))
        {
            return userController.handleRegister(request);
        }

        if (request.getMethod() == Method.POST && "/sessions".equals(request.getPath()))
        {
            return userController.handleLogin(request);
        }

        if (request.getMethod() == Method.POST && "/packages".equals(request.getPath()))
        {
            return packageController.handleCreatePackage(request);
        }
        if (request.getMethod() == Method.POST && "/transactions/packages".equals(request.getPath())) {
            return packageController.handleAcquirePackage(request);
        }
        if (request.getMethod() == Method.GET && "/cards".equals(request.getPath())) {
            return cardsController.handleGetCards(request);
        }

        // Wenn keine passende Route gefunden wird, senden Sie eine 404-Antwort zurück
        return new Response(HttpStatus.NOT_FOUND, ContentType.HTML, "Not Found");
    }
}
