package org.mtcg.app.models;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Getter
@Setter
public class TradeOffer
{
    @JsonProperty("Id")
    private String id;

    @JsonProperty("CardToTrade")
    private String cardToTrade;

    @JsonProperty("MinimumDamage")
    private int minimumDamage;

    @JsonProperty("Type")
    private String type;

    public TradeOffer() {}
}