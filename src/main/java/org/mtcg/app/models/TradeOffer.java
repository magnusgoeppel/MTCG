package org.mtcg.app.models;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Getter
@Setter
@JsonPropertyOrder({ "Id", "cardToTrade", "minimumDamage", "type"})
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

    public TradeOffer(String tradeId, String cardId, int minimumDamage, String type)
    {
        this.id = tradeId;
        this.cardToTrade = cardId;
        this.minimumDamage = minimumDamage;
        this.type = type;
    }
}