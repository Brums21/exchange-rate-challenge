package com.exchangeRateChallenge.exchangeRateAPI.models;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExchangeCurrency {
    
    private String fromCurrency;
    private String toCurrency;
    private Double rate;
}
