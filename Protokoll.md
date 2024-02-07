# Protokoll - Monster Trading Card Game (MTCG)

## Projektentwurf und technische Umsetzung

### Initialisierung und Serverstart
Die Main Klasse erstellt die Datenbanken Tabelle und startet den Server.

### Server- und Client-Management
Die Server Klasse hört auf eingehende Verbindungen auf dem Port 1001. Für jede eingehende Verbindung wird ein neuer ClientHandler Thread erzeugt. 
Der Server agiert als die zentrale Schnittstelle, die Client-Anfragen empfängt und die Bearbeitung an den ClientHandler weitergibt.

Der ClientHandler ist zuständig für die direkte Kommunikation mit dem Client. Er liest die eingehenden http-Anfragen und extrahiert die wichtigen 
Informationen wie die gewünschte Route, HTTP-Methode und Daten aus dem Request. Nach der Verarbeitung der Anfrage leitet der ClientHandler diese 
Informationen an den Router weiter.

### Anfrageverarbeitung und Routing
Die Request-Klasse repräsentiert eine eingehende HTTP-Anfrage. Sie enthält wichtige Informationen wie die HTTP-Methode, den Pfad der angeforderten Ressource, 
Header und den Body der Anfrage. Der ClientHandler nutzt diese Klasse, um eine strukturierte und leicht verständliche Repräsentation der eingehenden Anfrage 
zu schaffen.

Der Router empfängt die verarbeiteten Anfragedaten vom ClientHandler und entscheidet, welche Aktion basierend auf dem Pfad und der Methode der Anfrage ausgeführt 
werden soll. Er leitet die Anfrage an den entsprechenden Controller weiter, der für die spezifische Geschäftslogik zuständig ist. Der Router spielt eine zentrale 
Rolle in der Bestimmung des Flusses innerhalb der Anwendung.

### Antworterstellung
Der Response wird nach der Verarbeitung durch den entsprechenden Controller generiert. Diese Klasse repräsentiert die HTTP-Antwort und beinhaltet Statuscode, 
Inhaltstyp und den tatsächlichen Inhalt. Der Controller gibt die Response zurück zum Router, der sie an den ClientHandler weiterleitet. Der ClientHandler sendet 
dann die strukturierte Antwort an den Client. Diese Prozesskette gewährleistet eine klare Trennung der Verantwortlichkeiten und effiziente Antworterstellung.

### HTTP-Kommunikation
Die Methode Klasse beinhaltet ein Enum, das verschiedene HTTP-Methoden (GET, POST, PUT und DELETE). Sie dient dazu, den Typ der eingehenden Anfrage zu bestimmen.

Die HTTPStatus Klasse beinhaltet ein Enum, das mögliche HTTP-Statuscodes repräsentiert, wie 200 OK oder 404 NOT_FOUND. Sie wird für die Übermittlung des 
Anfrageergebnisses an den Client verwendet.

Die ContentType Klasse beinhaltet ein Enum, das verschiedene Arten von Content-Types (text/html, text/plain und application/json) definiert. Es dient dazu, den 
Clients das Format des übertragenen Inhalts mitzuteilen.

### Controller
Die Controller Klassen (User, Cards, Package, Game, Trading) beinhalten unterschiedliche Funktionalitäten. Jeder Controller empfängt Anfragen vom Router, 
führt Geschäftslogik aus und gibt entsprechende Antworten zurück.

### Service
Die Service Klassen (Auth, User, Cards, Package, Game, Trading) unterstützen die Controller durch Bereitstellung von Diensten und Hilfsfunktionen, die 
für Geschäftslogiken wie Benutzerverwaltung, Authentifizierung, Trading benötigt werden.

### Models
- Das **Card** Modell speichert wichtige Informationen über die Spielkarten, darunter Namen und Schaden, welche im Zentrum der Spielstrategie stehen.
- Das **Stats** Modell zeichnet die Spielstatistiken der Spieler auf, einschließlich ihrer Siege und Niederlagen, was für die Bewertung ihrer Leistung und für 
- Wettkämpfe relevant ist.
- Das **TradeOffer** Modell ist für das Verwalten von Handelsangeboten zuständig und hält Details zu den angebotenen und geforderten Karten fest, was den Spielern 
- ermöglicht, aktiv am Kartenmarkt teilzunehmen.
- Schließlich gibt es das **UserData** Modell, das persönliche Informationen der Spieler wie Namen und Biografie enthält, was vor allem für die Benutzerverwaltung 
- und Personalisierung des Spielerlebnisses wichtig ist.

### Unit-Tests
Verschiedene Funktionen werden durch Unit-Tests überprüft, um die Funktionalität und Sicherheit des Systems zu gewährleisten. Dazu gehören Tests für die 
Benutzerregistrierung, das Einloggen, das Aktualisieren von Benutzerdaten, das Verwalten von Spielkarten und vieles mehr.

### Unique Feature: Logout-Funktion
Die Logout-Funktion im Spiel ermöglicht Spielern, sich abzumelden. Diese benutzen dafür den Pfad /logout über eine POST-Methode. Die Funktion im UserController 
überprüft dann den Benutzer-Token und löscht ihn, was die Sitzung des Spielers beendet.

### Gewonnene Erkenntnisse
Während des Projekts habe ich die Wichtigkeit von sauberem und modularisiertem Code erkannt, praktische Erfahrung mit den Tools Curl, Java, PostgreSQL und 
Docker gesammelt und die Bedeutung und Anwendung von Unit-Tests kennengelernt.
