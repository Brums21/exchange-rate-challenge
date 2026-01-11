package com.exchangeRateChallenge.exchangeRateAPI.models;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/* Model class representing multiple exchange rates from a specific currency */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExchangeRates {
    
    private String fromCurrency;
    private Map<String, Double> rates;
}
