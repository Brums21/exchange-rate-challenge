package com.exchangeRateChallenge.exchangeRateAPI.models.DTOs;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ExchangeAPISymbolsDTO {

    @JsonProperty("currencies")
    private Map<String, String> symbols;

    public boolean hasSymbol(String currency) {
        return this.symbols.containsKey(currency);
    }

}
