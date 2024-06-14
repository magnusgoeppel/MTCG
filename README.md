# Monster Trading Card Game (MTCG)

## Project Description
The Monster Trading Card Game (MTCG) is a card game where players can collect, trade, and battle with cards against other players. Players create decks from their cards and use them to compete in battles.

## Features

### User Registration and Management
Users can register for the Monster Trading Card Game by providing a username and password. After registration, users can log in and receive a token for future requests. Players can edit their personal information such as name, bio, and picture. They can also log out, invalidating their authentication token.

### Card Collection and Deck Building
Players can acquire card packs to expand their collection. Each pack contains a random selection of cards. Players can view their collected cards and assemble a deck from them to use in battles against other players.

### Battle System
Players can battle against other players in the game. An automated system selects an opponent and conducts the battle. After the battle, players receive a detailed log showing the actions and the outcome. Players can view their own statistics such as the number of wins and losses, and there is a scoreboard where players are ranked by Elo rating.

### Card Trading System
Players have the option to create, view, and respond to trade offers in the game. They can offer their own cards for trade and set specific requirements for the trade. Players can view trade offers from other players and propose their own cards in exchange. They can also withdraw their own trade offers.

## Installation and Execution

### Prerequisites
- Java (JDK 11 or newer)
- Apache Maven (version 3.6.3 or newer)
- PostgreSQL database (version 12 or newer)

### Setup Steps

1. **Clone the Git Repository**
    - Open a terminal or command prompt.
    - Run the command: `git clone https://github.com/magnusgoeppel/MTCG`.

2. **Prepare the Database**
    - Create a new user named `admin` with the password `1234` in PostgreSQL, or adjust the `admin` and `password` variables in the `org.mtcg.Database.DatabaseSetup` file to match your PostgreSQL user data.
    - Create a new database named `mtcg` in PostgreSQL, ensuring that the user has all the necessary rights on the database.

3. **Compile the Project**
    - Navigate to the main directory of the project via the terminal or command prompt.
    - Run the command: `javac Main.java`.

4. **Start the Server**
    - Start the server with the command: `java org.mtcg.Main`.

### Running the Application
- After starting the server, you can access the REST API at the URL `http://localhost:10001/`.
- Use `curl` to send HTTP requests to the REST API. The `test` directory contains two `curl` scripts demonstrating the functionality of the REST API, one with HTTP header output and pauses between requests, and one without.
- The `test` directory also contains the file `mtcg-api.yaml`, which describes the REST API and provides an overview of the available endpoints.
