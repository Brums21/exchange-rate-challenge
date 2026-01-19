package com.exchangeratechallenge.exchangerateapi.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exchangeratechallenge.exchangerateapi.exceptions.BadRequestException;
import com.exchangeratechallenge.exchangerateapi.models.Conversion;
import com.exchangeratechallenge.exchangerateapi.models.DTOs.ExchangeAPIResponseDTO;
import com.exchangeratechallenge.exchangerateapi.models.DTOs.ExchangeAPISymbolsDTO;
import com.exchangeratechallenge.exchangerateapi.services.ConversionService;
import com.exchangeratechallenge.exchangerateapi.services.ExchangeExternalAPIService;

@ExtendWith(MockitoExtension.class)
class ConversionServiceTest {
    
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
        symbolsMap.put("AED", "United Arab Emirates Dirham");
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

        assertEquals(8.5, conversion.getConvertedCurrencies().get("EUR"), 0.001);
        assertEquals(36.72982, conversion.getConvertedCurrencies().get("GBP"), 0.001);
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

        assertEquals("Currency INVALID is not accepted", ex.getMessage());
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

        assertEquals("Currency INVALID is not accepted", ex.getMessage());
    }

    @Test
    void givenGetConversionValues_whenRateIsNotAvailable_thenThrowException(){

        String fromCurrency = "USD";
        String toCurrency = "EUR,AED";
        Double value = 10.0;

        ExchangeAPISymbolsDTO symbolsDTO = createSymbolsDTO();
        ExchangeAPIResponseDTO exchangeAPIResponseDTO = createExchangeAPIResponseDTO();

        when(exchangeExternalAPIService.getAcceptedSymbols()).thenReturn(symbolsDTO);
        when(exchangeExternalAPIService.getExchangeRate(fromCurrency)).thenReturn(exchangeAPIResponseDTO);

        BadRequestException ex = assertThrows(
            BadRequestException.class, 
            () -> conversionService.getConversionValues(fromCurrency, toCurrency, value)
        );

        assertEquals("Exchange rate not found from currency " + fromCurrency + " to currency AED", ex.getMessage());

    }

}
