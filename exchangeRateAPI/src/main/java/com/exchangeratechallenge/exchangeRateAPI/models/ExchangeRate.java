package com.exchangeratechallenge.exchangeRateAPI.models;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

/* Model class representing an exchange rate between two currencies */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExchangeRate {
    
    private String fromCurrency;
    private String toCurrency;
    private Double rate;
}
