package com.exchangeratechallenge.exchangeRateAPI.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import com.exchangeratechallenge.exchangeRateAPI.exceptions.BadRequestException;
import com.exchangeratechallenge.exchangeRateAPI.models.Conversion;
import com.exchangeratechallenge.exchangeRateAPI.models.DTOs.ExchangeAPIResponseDTO;
import com.exchangeratechallenge.exchangeRateAPI.models.DTOs.ExchangeAPISymbolsDTO;
import com.exchangeratechallenge.exchangeRateAPI.services.ExchangeExternalAPIService;
import com.exchangeratechallenge.exchangeRateAPI.services.ExchangeService;

@ExtendWith(MockitoExtension.class)
public class ConversionServiceTest {
    
    @Mock
    private ExchangeExternalAPIService exchangeExternalAPIService;

    @InjectMocks
    private ConversionService conversionService;

    private ExchangeAPISymbolsDTO createSymbolsDTO() {
        ExchangeAPISymbolsDTO symbolsDTO = new ExchangeAPISymbolsDTO();
        Map<String, String> symbolsMap = new HashMap<>();
        symbolsMap.put("USD", "United States Dollar");
        symbolsMap.put("EUR", "Euro");
        symbolsMap.put("GBP", "British Pound Sterling");
        symbolsDTO.setSymbols(symbolsMap);
        return symbolsDTO;
    }

    private ExchangeAPIResponseDTO createExchangeAPIResponseDTO() {
        Map<String, Double> ratesMap = new HashMap<>();
        ratesMap.put("USDEUR", 0.85);
        ratesMap.put("USDGBP", 3.672982);
        ratesMap.put("USDAFN", 57.8936);
        return new ExchangeAPIResponseDTO(ratesMap, "USD");
    }

    @Test
    void givenGetConversionValues_whenValidInput_thenReturnConversionValue() {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR,GBP";
        Double value = 10.0;

        ExchangeAPISymbolsDTO symbolsDTO = createSymbolsDTO();
        ExchangeAPIResponseDTO exchangeAPIResponseDTO = createExchangeAPIResponseDTO();

        when(exchangeExternalAPIService.getAcceptedSymbols()).thenReturn(symbolsDTO);
        when(exchangeExternalAPIService.getExchangeRate(fromCurrency)).thenReturn(exchangeAPIResponseDTO);

        Conversion conversion = conversionService.getConversionValues(fromCurrency, toCurrency, value);

        assertEquals(8.5, conversion.getConverterCurrencies().get("EUR"));
        assertEquals(36.72982, conversion.getConverterCurrencies().get("GBP"));
    }

    @Test
    void givenGetConversionValues_whenInvalidToCurrency_thenThrowException() {
        
        String fromCurrency = "USD";
        String toCurrency = "INVALID,EUR";
        Double value = 10.0;

        ExchangeAPISymbolsDTO symbolsDTO = createSymbolsDTO();
        ExchangeAPIResponseDTO exchangeAPIResponseDTO = createExchangeAPIResponseDTO();

        when(exchangeExternalAPIService.getAcceptedSymbols()).thenReturn(symbolsDTO);
        when(exchangeExternalAPIService.getExchangeRate(fromCurrency)).thenReturn(exchangeAPIResponseDTO);

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> conversionService.getConversionValues(fromCurrency, toCurrency, value)
        );

        assertEquals("Conversion not found for currency INVALID", ex.getMessage());
    }

    @Test
    void givenGetConversionValues_whenInvalidFromCurrency_thenThrowException() {
        
        String fromCurrency = "INVALID";
        String toCurrency = "EUR,GBP";
        Double value = 10.0;

        ExchangeAPISymbolsDTO symbolsDTO = createSymbolsDTO();

        when(exchangeExternalAPIService.getAcceptedSymbols()).thenReturn(symbolsDTO);

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> conversionService.getConversionValues(fromCurrency, toCurrency, value)
        );

        assertEquals("Conversion not found from currency INVALID", ex.getMessage());
    }

}
