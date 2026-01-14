package com.exchangeratechallenge.exchangeRateAPI.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.exchangeratechallenge.exchangeRateAPI.exceptions.BadRequestException;
import com.exchangeratechallenge.exchangeRateAPI.models.ExchangeRate;
import com.exchangeratechallenge.exchangeRateAPI.models.ExchangeRates;
import com.exchangeratechallenge.exchangeRateAPI.models.DTOs.ExchangeAPIResponseDTO;
import com.exchangeratechallenge.exchangeRateAPI.models.DTOs.ExchangeAPISymbolsDTO;
import com.exchangeratechallenge.exchangeRateAPI.utils.ParameterCleaner;

/**
 * Service class to handle exchange rate operations.
 */
@Service
public class ExchangeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeService.class);

    private final ExchangeExternalAPIService exchangeExternalAPIService;

    public ExchangeService(ExchangeExternalAPIService exchangeExternalAPIService) {
        this.exchangeExternalAPIService = exchangeExternalAPIService;
    }

    /**
     * Retrieves the exchange rate between two specified currencies.
     *
     * @param fromCurrency The currency code to convert from.
     * @param toCurrency   The currency code to convert to.
     * @return An ExchangeRate object containing the exchange rate details.
     * @throws BadRequestException if either currency is not accepted or the exchange rate is not found.
     */
    public ExchangeRate getExchangeRateFromToCurrency(String fromCurrency, String toCurrency) {

        ExchangeAPISymbolsDTO symbolsDTO = exchangeExternalAPIService.getAcceptedSymbols();

        fromCurrency = ParameterCleaner.cleanAndValidateCurrency(fromCurrency, symbolsDTO);
        toCurrency = ParameterCleaner.cleanAndValidateCurrency(toCurrency, symbolsDTO);

        LOGGER.info("Fetching exchange rate between two currencies.");

        ExchangeAPIResponseDTO exchangeDetailsDTO = exchangeExternalAPIService.getExchangeRate(fromCurrency);
        Double rate = exchangeDetailsDTO.getRates().get(toCurrency);

        if (rate == null) {
            throw new BadRequestException("Exchange rate not found from currency " + fromCurrency + " to currency " + toCurrency);
        }

        return new ExchangeRate(fromCurrency, toCurrency, rate);
    }

    /**
     * Retrieves all exchange rates from a specified currency.
     *
     * @param fromCurrency The currency code to convert from.
     * @return An ExchangeRates object containing all exchange rates from the specified currency.
     * @throws BadRequestException if the fromCurrency is not accepted.
     */
    public ExchangeRates getExchangeRatesFromCurrency(String fromCurrency) {
        
        ExchangeAPISymbolsDTO symbolsDTO = exchangeExternalAPIService.getAcceptedSymbols();

        fromCurrency = ParameterCleaner.cleanAndValidateCurrency(fromCurrency, symbolsDTO);

        LOGGER.info("Fetching all exchange rates from currency");
        ExchangeAPIResponseDTO exchangeDetailsDTO = exchangeExternalAPIService.getExchangeRate(fromCurrency);

        return new ExchangeRates(fromCurrency, exchangeDetailsDTO.getRates());
    }

}
