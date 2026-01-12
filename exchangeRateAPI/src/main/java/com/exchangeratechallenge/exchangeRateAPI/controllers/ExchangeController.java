package com.exchangeratechallenge.exchangeRateAPI.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exchangeratechallenge.exchangeRateAPI.models.ExchangeRate;
import com.exchangeratechallenge.exchangeRateAPI.models.ExchangeRates;
import com.exchangeratechallenge.exchangeRateAPI.services.ExchangeService;

/**
 * Controller class to handle exchange rate related endpoints.
 */
@RestController
@RequestMapping("/api/v1/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }
    
    /**
     * Endpoint to get the exchange rate between two currencies.
     *
     * @param fromCurrency The currency code to convert from.
     * @param toCurrency   The currency code to convert to.
     * @return An ExchangeRate object containing the exchange rate details.
     */
    @GetMapping("/rate")
    public ExchangeRate getExchangeRate(
        @RequestParam(name = "from", required = true) String fromCurrency,
        @RequestParam(name = "to", required = true) String toCurrency
    ) {
        return exchangeService.getExchangeRateFromToCurrency(fromCurrency, toCurrency);
    }

    /**
     * Endpoint to get all exchange rates from a specific currency.
     *
     * @param fromCurrency The currency code to convert from.
     * @return An ExchangeRates object containing all exchange rates from the specified currency.
     */
    @GetMapping("/rates")
    public ExchangeRates getExchangeRates(
        @RequestParam(name = "from", required = true) String fromCurrency
    ) {
        return exchangeService.getExchangeRatesFromCurrency(fromCurrency);
    }
}
