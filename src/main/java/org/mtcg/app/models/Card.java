package org.mtcg.app.models;

import lombok.Getter;

@Getter
public class Card
{
    private String id;
    private String name;
    private double damage;
    private String elementType;
    private String type;

    public Card(String id, String name, double damage, String elementType, String type)
    {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.type = type;
    }
}

