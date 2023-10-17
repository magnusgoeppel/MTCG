package org.mtcg.app.models;

import lombok.Getter;

@Getter
public class Card
{
    public Card(String name, int damage, String elementType)
    {
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
    }

    private String name;
    private int damage;
    private String elementType;

}

