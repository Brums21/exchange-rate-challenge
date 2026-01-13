package com.exchangeratechallenge.exchangeRateAPI.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class to handle exchange rate conversion related endpoints.
 */
@RestController
@RequestMapping("/api/v1/convert")
public class ConversionController {
    
    //private final ExchangeService exchangeService;

    public ConversionController() {
        //this.exchangeService = exchangeService;
    }

    @GetMapping("/currency")
    public Object getExchangeRate(
        @RequestParam(name = "from", required = true) String fromCurrency,
        @RequestParam(name = "to", required = true) String toCurrency
    ) {

        return null;
    }
}
