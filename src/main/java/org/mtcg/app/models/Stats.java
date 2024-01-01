package org.mtcg.app.models;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Getter
@Setter
//@JsonPropertyOrder({ "Username", "Elo", "Wins", "Losses"})
public class Stats
{
    private String username;
    private int elo;
    private int wins;
    private int losses;

    public Stats(int elo, int wins, int losses, String username)
    {
        this.elo = elo;
        this.wins = wins;
        this.losses = losses;
        this.username = username;
    }
}
