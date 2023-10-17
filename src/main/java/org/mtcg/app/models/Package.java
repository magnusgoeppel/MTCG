package org.mtcg.app.models;

import java.util.List;

public class Package
{
    private List<Card> cards;

    public Package() {
    }

    public void buyPackage(User user)
    {

    }

    public void addCard(Card card)
    {
        cards.add(card);
    }
}

