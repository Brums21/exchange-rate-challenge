package com.exchangeratechallenge.exchangerateapi.controllers;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exchangeratechallenge.exchangerateapi.models.Conversion;
import com.exchangeratechallenge.exchangerateapi.services.ConversionService;

import jakarta.validation.constraints.Positive;

/**
 * Controller class to handle exchange rate conversion related endpoints.
 */
@RestController
@RequestMapping("/api/v1/convert")
@Validated
public class ConversionController {
    
    private final ConversionService conversionService;

    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Endpoint to get the conversion value from one currency to one or several currencies
     * 
     * @param from  The currency code to convert from.
     * @param to    The currency code(s) to convert to.
     * @param value The value to convert to.
     * @return A conversion object containing the converted values.
    */
    @GetMapping("/currency")
    public Conversion getExchangeRate(
        @RequestParam(name = "from", required = true) String fromCurrency,
        @RequestParam(name = "to", required = true) String toCurrency,
        @RequestParam(name = "value", required = true) @Positive(message = "Value must be greater than 0") Double value
    ) {

        return conversionService.getConversionValues(fromCurrency, toCurrency, value);
    }
}
