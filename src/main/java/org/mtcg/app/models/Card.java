package org.mtcg.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Card
{
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Damage")
    private double damage;
    private String elementType;
    private String type;

    public Card()
    {
    }

    public Card(String id, String name, double damage, String elementType, String type)
    {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.type = type;
    }
}

