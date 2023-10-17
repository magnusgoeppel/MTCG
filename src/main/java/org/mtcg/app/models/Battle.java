package org.mtcg.app.models;

import lombok.Getter;

@Getter
public class Battle
{
    private User user1;
    private User user2;

    private String log;

    public Battle(User user1, User user2)
    {
        this.user1 = user1;
        this.user2 = user2;
        this.log = "";
    }

    public void startBattle()
    {

    }
}