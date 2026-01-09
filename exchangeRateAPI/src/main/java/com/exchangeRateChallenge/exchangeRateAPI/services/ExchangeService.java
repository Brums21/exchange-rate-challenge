package com.exchangeRateChallenge.exchangeRateAPI.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.exchangeRateChallenge.exchangeRateAPI.exceptions.BadRequestException;
import com.exchangeRateChallenge.exchangeRateAPI.models.ExchangeRate;
import com.exchangeRateChallenge.exchangeRateAPI.models.ExchangeRates;
import com.exchangeRateChallenge.exchangeRateAPI.models.DTOs.ExchangeAPISymbolsDTO;
import com.exchangeRateChallenge.exchangeRateAPI.models.DTOs.ExchangeAPIResponseDTO;

@Service
public class ExchangeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeService.class);

    private final ExchangeExternalAPIService exchangeExternalAPIService;

    public ExchangeService(ExchangeExternalAPIService exchangeExternalAPIService) {
        this.exchangeExternalAPIService = exchangeExternalAPIService;
    }

    public ExchangeRate getExchangeRateFromToCurrency(String fromCurrency, String toCurrency) {

        ExchangeAPISymbolsDTO symbolsDTO = exchangeExternalAPIService.getAcceptedSymbols();

        fromCurrency = cleanAndValidateCurrency(fromCurrency, symbolsDTO);
        toCurrency = cleanAndValidateCurrency(toCurrency, symbolsDTO);

        LOGGER.info("Fetching exchange rate from {} to {}", fromCurrency, toCurrency);

        ExchangeAPIResponseDTO exchangeDetailsDTO = exchangeExternalAPIService.getExchangeRate(fromCurrency);
        Double rate = exchangeDetailsDTO.getRates().get(toCurrency);

        if (rate == null) {
            throw new BadRequestException("Exchange rate not found from currency " + fromCurrency + " to currency " + toCurrency);
        }

        return new ExchangeRate(fromCurrency, toCurrency, rate);
    }

    public ExchangeRates getExchangeRatesFromCurrency(String fromCurrency) {
        
        ExchangeAPISymbolsDTO symbolsDTO = exchangeExternalAPIService.getAcceptedSymbols();

        fromCurrency = cleanAndValidateCurrency(fromCurrency, symbolsDTO);

        LOGGER.info("Fetching exchange rates from {}", fromCurrency);
        ExchangeAPIResponseDTO exchangeDetailsDTO = exchangeExternalAPIService.getExchangeRate(fromCurrency);

        return new ExchangeRates(fromCurrency, exchangeDetailsDTO.getRates());
    }

    private static String cleanString(String currency) {
        return currency.trim()
            .replaceAll("^\"+", "")
            .replaceAll("\"+$", "")
            .replaceAll("\'+", "")
            .replaceAll("\'+$", "")
            .toUpperCase();
    }

    private static String cleanAndValidateCurrency(String currency, ExchangeAPISymbolsDTO symbolsDTO) {
        currency = cleanString(currency);
        if (!symbolsDTO.hasSymbol(currency)) {
            throw new BadRequestException("Currency " + currency + " is not accepted");
        }

        return currency;
    }


}
