package org.mtcg.app.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public TradeOffer()
    {

    }
}
