package com.exchangeratechallenge.exchangeRateAPI.models.DTOs;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/* DTO for the exchange API symbols response */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ExchangeAPISymbolsDTO {

    @JsonProperty("currencies")
    private Map<String, String> symbols;

    /** 
     * Checks if a given currency symbol exists in the symbols map
     * @param currency The currency symbol to check
     * @return true if the symbol exists, false otherwise
    */
    public boolean hasSymbol(String currency) {
        return this.symbols.containsKey(currency);
    }

}
