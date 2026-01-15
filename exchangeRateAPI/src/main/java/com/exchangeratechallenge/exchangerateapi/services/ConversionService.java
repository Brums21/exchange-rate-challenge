package com.exchangeratechallenge.exchangerateapi.services;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exchangeratechallenge.exchangerateapi.exceptions.BadRequestException;
import com.exchangeratechallenge.exchangerateapi.models.Conversion;
import com.exchangeratechallenge.exchangerateapi.models.DTOs.ExchangeAPIResponseDTO;
import com.exchangeratechallenge.exchangerateapi.models.DTOs.ExchangeAPISymbolsDTO;
import com.exchangeratechallenge.exchangerateapi.utils.ParameterCleaner;

/**
 * Service class to handle conversion operations.
 */
@Service
public class ConversionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionService.class);

    private final ExchangeExternalAPIService exchangeExternalAPIService;

    public ConversionService(ExchangeExternalAPIService exchangeExternalAPIService) {
        this.exchangeExternalAPIService = exchangeExternalAPIService;
    }

    /**
     * Retrieves the conversion from a specific currency to a comma-separated list of currencies.
     *
     * @param fromCurrency The currency code to convert from.
     * @param toCurrency Comma-seperated string with the currency code(s) to convert to.
     * @return A Conversion object containing the conversion details.
     * @throws BadRequestException if either currency is not accepted or the exchange rate is not found for one of currencies.
     */
    public Conversion getConversionValues(String fromCurrency, String toCurrencies, Double value) {

        LOGGER.info("Fetching conversion values for user-defined currencies.");

        ExchangeAPISymbolsDTO symbolsDTO = exchangeExternalAPIService.getAcceptedSymbols();

        fromCurrency = ParameterCleaner.cleanAndValidateCurrency(fromCurrency, symbolsDTO);
        ExchangeAPIResponseDTO exchangeRateDTO = exchangeExternalAPIService.getExchangeRate(fromCurrency);

        Map<String, Double> conversionMap = new HashMap<>();

        for (String element : toCurrencies.split(",")) {
            
            String cleanedCurrency = ParameterCleaner.cleanAndValidateCurrency(element, symbolsDTO);
            Double exchangeRate = exchangeRateDTO.getRates().get(cleanedCurrency);

             if (exchangeRate == null) {
                throw new BadRequestException("Exchange rate not found from currency " + fromCurrency + " to currency " + cleanedCurrency);
            }

            conversionMap.put(cleanedCurrency, value * exchangeRate);
        }

        return new Conversion(fromCurrency, value, conversionMap);
    }

}
