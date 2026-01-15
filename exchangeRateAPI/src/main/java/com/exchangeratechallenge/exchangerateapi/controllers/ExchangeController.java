package com.exchangeratechallenge.exchangerateapi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exchangeratechallenge.exchangerateapi.models.ExchangeRate;
import com.exchangeratechallenge.exchangerateapi.models.ExchangeRates;
import com.exchangeratechallenge.exchangerateapi.services.ExchangeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller class to handle exchange rate related endpoints.
 */
@Tag(name = "Exchange Controller")
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
    @Operation(summary = "Get exchange rate between two currencies")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Successfully return the exchange rate"),
        @ApiResponse(responseCode = "400", description = "Bad request made"), 
        @ApiResponse(responseCode = "502", description = "External API error")
    })
    @GetMapping("/rate")
    public ExchangeRate getExchangeRate(
        @Parameter(description = "Currency code to convert from") @RequestParam(name = "from", required = true) String fromCurrency,
        @Parameter(description = "Currency code to convert to") @RequestParam(name = "to", required = true) String toCurrency
    ) {
        return exchangeService.getExchangeRateFromToCurrency(fromCurrency, toCurrency);
    }

    /**
     * Endpoint to get all exchange rates from a specific currency.
     *
     * @param fromCurrency The currency code to convert from.
     * @return An ExchangeRates object containing all exchange rates from the specified currency.
     */
    @Operation(summary = "Get exchange rate from one currency to several currencies")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Successfully return the conversions"),
        @ApiResponse(responseCode = "400", description = "Bad request made"), 
        @ApiResponse(responseCode = "502", description = "External API error")
    })
    @GetMapping("/rates")
    public ExchangeRates getExchangeRates(
        @Parameter(description = "Currency code to convert from") @RequestParam(name = "from", required = true) String fromCurrency
    ) {
        return exchangeService.getExchangeRatesFromCurrency(fromCurrency);
    }
}
