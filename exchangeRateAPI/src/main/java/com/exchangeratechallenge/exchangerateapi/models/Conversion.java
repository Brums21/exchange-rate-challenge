package com.exchangeratechallenge.exchangerateapi.models;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Conversion {
    
    private String fromCurrency;
    private Double originalValue;
    private Map<String, Double> converterCurrencies;

}
