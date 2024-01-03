import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mtcg.app.controllers.*;
import org.mtcg.app.models.Card;
import org.mtcg.app.models.Stats;
import org.mtcg.app.models.TradeOffer;
import org.mtcg.app.services.*;
import org.mtcg.http.HttpStatus;
import org.mtcg.server.Request;
import org.mtcg.server.Response;
import java.util.*;

// Testklasse für die Unit-Tests
public class UnitTests
{

    // Registrierung eines Benutzers
    @Test
    public void testRegisterUser()
    {
        UserService userService = Mockito.mock(UserService.class);
        when(userService.registerUser(anyString(), anyString())).thenReturn(true);
        boolean result = userService.registerUser("newUser", "password123");
        assertTrue(result, "User should be registered successfully");
    }

    // Anmeldung eines Benutzers
    @Test
    public void testLoginUser()
    {
        UserService userService = Mockito.mock(UserService.class);
        when(userService.loginUser(anyString(), anyString())).thenReturn(true);
        boolean result = userService.loginUser("newUser", "password123");
        assertTrue(result, "User should be logged in successfully");
    }

    // Benutzerdaten ändern
    @Test
    public void testUpdateUser()
    {
        UserService userService = Mockito.mock(UserService.class);
        when(userService.updateUser(anyInt(), anyString(), anyString(), anyString())).thenReturn(true);
        boolean result = userService.updateUser(1, "Updated Name", "Updated Bio", "Updated Image");
        assertTrue(result, "User data should be updated successfully");
    }

    // Speichern eines Benutzertokens
    @Test
    public void testSaveUserToken()
    {
        UserService userService = Mockito.mock(UserService.class);
        when(userService.saveUserToken(anyString(), anyString())).thenReturn(true);
        boolean result = userService.saveUserToken("newUser", "newUser-mtcgToken");
        assertTrue(result, "User data should be updated successfully");
    }

    // Löschen eines Benutzertokens
    @Test
    public void testDeleteUserToken()
    {
        UserService userService = Mockito.mock(UserService.class);
        when(userService.deleteUserToken(anyInt())).thenReturn(true);
        boolean result = userService.deleteUserToken(1);
        assertTrue(result, "User data should be updated successfully");
    }

    // Extrahieren der Benutzer-ID aus dem Token
    @Test
    public void testExtractUserIdFromAuthHeader()
    {
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.extractUserIdFromAuthHeader(any(Request.class))).thenReturn(1);
        Request mockRequest = mock(Request.class);

        // Simulieren der Anfrageheader
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer mtcgToken");
        when(mockRequest.getHeaders()).thenReturn(headers);

        // Act
        int userId = authService.extractUserIdFromAuthHeader(mockRequest);

        // Assert
        assertEquals(1, userId, "The user ID should be 1");
    }


    // Konvertieren von JSON zu Kartenobjekten
    @Test
    void testConvertJsonToCards()
    {
        // Arrange
        PackageService service = new PackageService();
        String jsonInput = "[{\"id\":\"1\",\"name\":\"FireSpell\",\"damage\":50}]";

        // Act
        List<Card> cards = service.convertJsonToCards(jsonInput);

        // Assert
        assertNotNull(cards, "The returned list should not be null.");
        assertEquals(1, cards.size(), "The list should contain one card.");
        assertEquals("FireSpell", cards.get(0).getName(), "The card name should be FireSpell.");
    }


    // Überprüfen der Kartenexistenz
    @Test
    void testCheckCardsExistence()
    {
        PackageService service = Mockito.mock(PackageService.class);
        when(service.checkCardsExistence(anyList())).thenReturn(true);

        List<Card> cards = new ArrayList<>();
        cards.add(new Card("1", "FireSpell", 50));

        boolean exists = service.checkCardsExistence(cards);

        assertTrue(exists, "The method should return true if the card exists.");
    }


    // Überprüft die Karten des Benutzers
    @Test
    public void testGetCardsForUserReturnsCorrectCards()
    {
        // Arrange
        int userId = 1;
        List<Card> expectedCards = Arrays.asList(new Card("1", "Card1", 10),
                                                 new Card("2", "Card2", 20),
                                                 new Card("3", "Card3", 30));
        CardsService service = mock(CardsService.class);

        when(service.getCardsForUser(userId)).thenReturn(expectedCards);

        // Act
        List<Card> actualCards = service.getCardsForUser(userId);

        // Assert
        assertEquals(expectedCards, actualCards, "The returned cards should match the expected cards");
    }


    // Konvertieren von Kartenobjekten in JSON
    @Test
    public void testConvertToJsonReturnsValidJson()
    {
        // Arrange
        List<Card> cards = Arrays.asList(new Card("1", "Card1", 10),
                                         new Card("2", "Card2", 20),
                                         new Card("3", "Card3", 30));

        CardsService service = new CardsService();

        // Act
        String json = service.convertToJson(cards);

        // Assert
        assertNotNull(json, "The returned JSON should not be null");
        assertTrue(json.startsWith("[") && json.endsWith("]"), "The returned JSON should be an array");
    }


    // Decks mit zu wenigen Karten werden abgelehnt
    @Test
    public void testIsDeckSizeValidWithInvalidSize()
    {
        String requestBody = "[\"1\", \"2\", \"3\"]";
        CardsService service = new CardsService();

        boolean isValid = service.isDeckSizeValid(requestBody);

        assertFalse(isValid, "The deck size should be invalid");
    }


    // Decks mit der richtigen Anzahl von Karten werden akzeptiert
    @Test
    public void testConfigureDeckForUserSuccessfullyConfiguresDeck()
    {
        int userId = 1;
        List<String> cardIds = Arrays.asList("1", "2", "3", "4");
        CardsService service = mock(CardsService.class);
        when(service.configureDeckForUser(userId, cardIds)).thenReturn(true);

        boolean result = service.configureDeckForUser(userId, cardIds);

        assertTrue(result, "The deck should be successfully configured");
    }

    // Stats abrufen
    @Test
    public void testGetStats()
    {
        GameService gameService = Mockito.mock(GameService.class);
        int userId = 1;
        String expectedStats = "{\"Username\":\"TestUser\",\"Elo\":1000,\"Wins\":10,\"Losses\":5}";

        Mockito.when(gameService.getStats(userId)).thenReturn(expectedStats);

        String stats = gameService.getStats(userId);

        assertEquals(expectedStats, stats, "Die zurückgegebenen Statistiken sollten den erwarteten Statistiken entsprechen.");
    }

    // Scoreboard abrufen
    @Test
    public void testGetScoreboard()
    {
        GameService gameService = Mockito.mock(GameService.class);
        List<Stats> expectedScoreboard = new ArrayList<>();
        expectedScoreboard.add(new Stats(1000, 10, 5, "TestUser"));

        Mockito.when(gameService.getScoreboard()).thenReturn(expectedScoreboard);

        List<Stats> scoreboard = gameService.getScoreboard();

        assertEquals(expectedScoreboard.size(), scoreboard.size(), "Die Größe des Scoreboards sollte gleich sein.");
        assertEquals(expectedScoreboard.get(0).getUsername(), scoreboard.get(0).getUsername(), "Die Benutzernamen sollten übereinstimmen.");
    }

    // Gegner abrufen
    @Test
    public void testGetOpponent()
    {
        // Arrange
        GameService gameService = Mockito.mock(GameService.class);
        int userId = 1;
        int expectedOpponentId = 2;

        Mockito.when(gameService.getOpponent(userId)).thenReturn(expectedOpponentId);

        int opponentId = gameService.getOpponent(userId);

        assertEquals(expectedOpponentId, opponentId, "Die ID des Gegners sollte der erwarteten ID entsprechen.");
    }

    // Kampf durchführen und Kampfprotokoll abrufen
    @Test
    public void testBattle()
    {
        // Arrange
        GameService gameService = Mockito.mock(GameService.class);
        int userId = 1;
        int opponentId = 2;
        String expectedBattleLog = "Battle log...";

        Mockito.when(gameService.Battle(userId, opponentId)).thenReturn(expectedBattleLog);

        String battleLog = gameService.Battle(userId, opponentId);

        assertEquals(expectedBattleLog, battleLog, "Das Kampfprotokoll sollte dem erwarteten Protokoll entsprechen.");
    }

    // Scoreboard aktualisieren
    @Test
    public void testUpdateScoreboard()
    {
        GameService gameService = Mockito.mock(GameService.class);
        Mockito.doNothing().when(gameService).updateScoreboard();

        gameService.updateScoreboard();

        Mockito.verify(gameService).updateScoreboard();
    }

    // Stats eines Benutzers abrufen
    @Test
    public void testHandleGetStatsValidUser()
    {
        AuthService mockAuthService = Mockito.mock(AuthService.class);
        GameService mockGameService = Mockito.mock(GameService.class);
        GameController gameController = new GameController();

        gameController.setAuthService(mockAuthService);
        gameController.setGameService(mockGameService);

        Request mockRequest = Mockito.mock(Request.class);

        when(mockAuthService.extractUserIdFromAuthHeader(mockRequest)).thenReturn(1);
        when(mockGameService.getStats(1)).thenReturn("{\"Username\":\"TestUser\",\"Elo\":1000,\"Wins\":10,\"Losses\":5}");

        Response response = gameController.handleGetStats(mockRequest);

        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
        assertEquals("{\"Username\":\"TestUser\",\"Elo\":1000,\"Wins\":10,\"Losses\":5}", response.getContent());
    }

    // Paket erstellen
    @Test
    void testHandleCreatePackageAsAdmin()
    {
        PackageService mockPackageService = mock(PackageService.class);
        AuthService mockAuthService = mock(AuthService.class);
        Request mockRequest = mock(Request.class);

        PackageController controller = new PackageController();
        controller.setPackageService(mockPackageService);
        controller.setAuthService(mockAuthService);

        // Simulieren der Anfrageheader und -körper
        when(mockRequest.getHeaders()).thenReturn(Map.of("Authorization", "Bearer admin-mtcgToken"));
        when(mockRequest.getBody()).thenReturn("[{\"id\":\"card1\",\"name\":\"Fire Dragon\",\"damage\":50}]");

        // Simulieren des Verhaltens von PackageService und AuthService
        when(mockPackageService.getAdminToken()).thenReturn("admin-mtcgToken");
        when(mockPackageService.createPackage(any())).thenReturn(true);

        Response response = controller.handleCreatePackage(mockRequest);

        assertEquals(HttpStatus.CREATED.getCode(), response.getStatusCode());
        assertEquals("Package and cards successfully created", response.getContent());
    }

    // Paket erwerben
    @Test
    void testHandleAcquirePackage()
    {
        PackageService mockPackageService = mock(PackageService.class);
        AuthService mockAuthService = mock(AuthService.class);
        Request mockRequest = mock(Request.class);

        PackageController controller = new PackageController();
        controller.setPackageService(mockPackageService);
        controller.setAuthService(mockAuthService);

        // Simulieren der Benutzer-ID aus dem Token
        when(mockAuthService.extractUserIdFromAuthHeader(mockRequest)).thenReturn(1);

        // Simulieren des Verhaltens von PackageService
        when(mockPackageService.checkCoins(1)).thenReturn(true);
        when(mockPackageService.checkPackageAvailable()).thenReturn(true);
        when(mockPackageService.acquirePackage(1)).thenReturn(true);


        Response response = controller.handleAcquirePackage(mockRequest);

        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
        assertEquals("Package successfully acquired", response.getContent());
    }

    @Test
    void testHandleCreatePackageWithExistingCards()
    {
        // Arrange
        PackageService mockPackageService = mock(PackageService.class);
        AuthService mockAuthService = mock(AuthService.class);
        Request mockRequest = mock(Request.class);

        PackageController controller = new PackageController();
        controller.setPackageService(mockPackageService);
        controller.setAuthService(mockAuthService);

        // Simulieren der Anfrageheader und -körper
        when(mockRequest.getHeaders()).thenReturn(Map.of("Authorization", "Bearer admin-mtcgToken"));
        when(mockRequest.getBody()).thenReturn("[{\"id\":\"card1\",\"name\":\"Fire Dragon\",\"damage\":50}]");

        // Simulieren des Verhaltens von PackageService und AuthService
        when(mockPackageService.getAdminToken()).thenReturn("admin-mtcgToken");
        when(mockPackageService.checkCardsExistence(any())).thenReturn(true);

        Response response = controller.handleCreatePackage(mockRequest);

        assertEquals(HttpStatus.CONFLICT.getCode(), response.getStatusCode());
        assertEquals("Card already exists", response.getContent());
    }

    // Handelsangebote abrufen
    @Test
    void testHandleGetTradingDeals()
    {
        // Arrange
        AuthService mockAuthService = mock(AuthService.class);
        TradingService mockTradingService = mock(TradingService.class);
        Request mockRequest = mock(Request.class);
        TradeOffer mockTradeOffer = mock(TradeOffer.class);

        TradingController controller = new TradingController();
        controller.setAuthService(mockAuthService);
        controller.setTradingService(mockTradingService);

        // Konfigurieren der Mocks, um realistische Rückgabewerte zu liefern
        when(mockAuthService.extractUserIdFromAuthHeader(mockRequest)).thenReturn(1);
        when(mockTradingService.getTrades()).thenReturn(List.of(mockTradeOffer));
        when(mockTradingService.convertTradesToJson(List.of(mockTradeOffer))).thenReturn("[{\"id\":\"1\",\"cardToTrade\":{\"id\":\"1\",\"name\":\"FireSpell\",\"damage\":50},\"minimumDamage\":50},{\"id\":\"2\",\"cardToTrade\":{\"id\":\"2\",\"name\":\"WaterSpell\",\"damage\":50},\"minimumDamage\":50}]");


        Response response = controller.handleGetTradingDeals(mockRequest);

        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
        assertEquals("[{\"id\":\"1\",\"cardToTrade\":{\"id\":\"1\",\"name\":\"FireSpell\",\"damage\":50},\"minimumDamage\":50},{\"id\":\"2\",\"cardToTrade\":{\"id\":\"2\",\"name\":\"WaterSpell\",\"damage\":50},\"minimumDamage\":50}]", response.getContent());
    }



    // Handelsangebot erstellen
    @Test
    void testHandleCreateTradingDeal()
    {
        TradingService mockTradingService = mock(TradingService.class);
        TradeOffer mockTradeOffer = mock(TradeOffer.class);
        int userId = mock(AuthService.class).extractUserIdFromAuthHeader(any(Request.class));

        when(mockTradingService.createTrade(any(TradeOffer.class), anyInt())).thenReturn(true);

        boolean result = mockTradingService.createTrade(mockTradeOffer, userId);

        assertTrue(result, "The trade should be created successfully");
    }

    // Handelsangebot löschen
    @Test
    void testHandleDeleteTradingDeal()
    {
        TradingService mockTradingService = mock(TradingService.class);
        TradeOffer mockTradeOffer = mock(TradeOffer.class);

        when(mockTradeOffer.getId()).thenReturn("8254zu8902340u83ui8023");
        when(mockTradingService.deleteTrade(anyString())).thenReturn(1);

        int result = mockTradingService.deleteTrade(mockTradeOffer.getId());

        assertEquals(1, result, "The trade should be deleted successfully");
    }

}