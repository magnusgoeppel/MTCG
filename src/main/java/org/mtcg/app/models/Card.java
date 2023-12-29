package org.mtcg.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@JsonPropertyOrder({ "Id", "Name", "Damage"})
@Getter
public class Card
{
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Damage")
    private int damage;


    public Card()
    {
    }



    public Card(String id, String name, int damage)
    {
        this.id = id;
        this.name = name;
        this.damage = damage;
    }
}

