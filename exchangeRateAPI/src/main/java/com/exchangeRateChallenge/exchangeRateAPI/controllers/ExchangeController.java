package com.exchangeRateChallenge.exchangeRateAPI.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exchangeRateChallenge.exchangeRateAPI.models.ExchangeCurrency;
import com.exchangeRateChallenge.exchangeRateAPI.services.ExchangeAPIService;

@RestController
@RequestMapping("/api/v1/exchange")
public class ExchangeController {

    private final ExchangeAPIService exchangeAPIService;

    public ExchangeController(ExchangeAPIService exchangeAPIService) {
        this.exchangeAPIService = exchangeAPIService;
    }
    
    @GetMapping("/exchange-rate")
    public ExchangeCurrency getExchangeRate(
        @RequestParam(name = "from", required = true) String fromCurrency,
        @RequestParam(name = "to", required = true) String toCurrency
    ) {

        ExchangeCurrency exchangeDetails = exchangeAPIService.getExchangeRateFromToCurrency(fromCurrency, toCurrency);

        return exchangeDetails;
    }
}
