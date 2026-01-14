package com.exchangeratechallenge.exchangeRateAPI.services;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exchangeratechallenge.exchangeRateAPI.exceptions.BadRequestException;
import com.exchangeratechallenge.exchangeRateAPI.models.Conversion;
import com.exchangeratechallenge.exchangeRateAPI.models.DTOs.ExchangeAPIResponseDTO;
import com.exchangeratechallenge.exchangeRateAPI.models.DTOs.ExchangeAPISymbolsDTO;
import com.exchangeratechallenge.exchangeRateAPI.utils.ParameterCleaner;

@Service
public class ConversionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionService.class);

    private final ExchangeExternalAPIService exchangeExternalAPIService;

    public ConversionService(ExchangeExternalAPIService exchangeExternalAPIService) {
        this.exchangeExternalAPIService = exchangeExternalAPIService;
    }

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
