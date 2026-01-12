package com.exchangeRateChallenge.exchangeRateAPI.models.DTOs;

import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.ToString;

/* DTO for the exchange API response */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ExchangeAPIResponseDTO {

    @JsonProperty("quotes")
    private Map<String, Double> rates;

    @JsonProperty("source")
    private String sourceCurrency;

    /* Constructor that processes the rates map to remove the source currency prefix */
    public ExchangeAPIResponseDTO(Map<String, Double> rates, String sourceCurrency) {
        
        this.rates = rates.entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> e.getKey().substring(3),
                    Map.Entry::getValue
                )
            );

        this.sourceCurrency = sourceCurrency;
    }
    
}
