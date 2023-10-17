package org.mtcg.app.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class User
{
    private String username;
    private String password;
    private int coins;
    private int eloValue;
    private String token;
    private Deck deck;

    public User(String username, String password, int coins, int eloValue, String token, Deck deck)
    {
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.eloValue = eloValue;
        this.token = token;
        this.deck = deck;
    }


    public void register()
    {

    }

    public void login()
    {

    }

    public void editProfile()
    {

    }

    public void viewStats()
    {

    }

    public void updateELO(boolean win)
    {

    }

    public boolean checkToken(String token)
    {
        return false;
    }
}

