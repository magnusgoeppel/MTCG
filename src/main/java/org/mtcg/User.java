package org.mtcg;

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

