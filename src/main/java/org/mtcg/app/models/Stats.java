package org.mtcg.app.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({ "Username", "Elo", "Wins", "Losses"})
@Getter
@Setter
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
