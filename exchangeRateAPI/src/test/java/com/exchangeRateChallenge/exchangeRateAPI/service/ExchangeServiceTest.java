package com.exchangeRateChallenge.exchangeRateAPI.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exchangeRateChallenge.exchangeRateAPI.exceptions.BadRequestException;
import com.exchangeRateChallenge.exchangeRateAPI.models.ExchangeRate;
import com.exchangeRateChallenge.exchangeRateAPI.models.DTOs.ExchangeAPIResponseDTO;
import com.exchangeRateChallenge.exchangeRateAPI.models.DTOs.ExchangeAPISymbolsDTO;
import com.exchangeRateChallenge.exchangeRateAPI.services.ExchangeExternalAPIService;
import com.exchangeRateChallenge.exchangeRateAPI.services.ExchangeService;

@ExtendWith(MockitoExtension.class)
public class ExchangeServiceTest {

    @Mock
    private ExchangeExternalAPIService exchangeExternalAPIService;
    
    @InjectMocks
    private ExchangeService exchangeService;

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
        ratesMap.put("USDAED", 3.672982);
        ratesMap.put("USDAFN", 57.8936);
        return new ExchangeAPIResponseDTO(ratesMap, "USD");
    }

    @Test
    public void givenGetExchangeRateFromToCurrency_whenValidInput_thenReturnExchangeRate() {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR";

        ExchangeAPISymbolsDTO symbolsDTO = createSymbolsDTO();
        ExchangeAPIResponseDTO exchangeAPIResponseDTO = createExchangeAPIResponseDTO();

        when(exchangeExternalAPIService.getAcceptedSymbols()).thenReturn(symbolsDTO);
        when(exchangeExternalAPIService.getExchangeRate(fromCurrency)).thenReturn(exchangeAPIResponseDTO);
        
        ExchangeRate exchangeCurrent = exchangeService.getExchangeRateFromToCurrency(fromCurrency, toCurrency);
        assertTrue(exchangeCurrent.getRate() == 0.85);
        assertTrue(exchangeCurrent.getFromCurrency().equals("USD"));
        assertTrue(exchangeCurrent.getToCurrency().equals("EUR"));
    
    }

    @Test
    public void givenGetExchangeRateFromToCurrency_whenInvalidToCurrencyProvided_thenThrowBadRequestException() {
        
        String fromCurrencyValid = "USD";
        String toCurrencyInvalid = "INVALID";

        ExchangeAPISymbolsDTO symbolsDTO = createSymbolsDTO();

        when(exchangeExternalAPIService.getAcceptedSymbols()).thenReturn(symbolsDTO);

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> exchangeService.getExchangeRateFromToCurrency(fromCurrencyValid, toCurrencyInvalid)
        );

        assertTrue(ex.getMessage().equals("Currency " + toCurrencyInvalid + " is not accepted"));

        String fromCurrencyInvalid = "INVALID";
        String toCurrencyValid = "EUR";

        BadRequestException ex2 = assertThrows(
            BadRequestException.class,
            () -> exchangeService.getExchangeRateFromToCurrency(fromCurrencyInvalid, toCurrencyValid)
        );

        assertTrue(ex2.getMessage().equals("Currency " + fromCurrencyInvalid + " is not accepted"));
    }

    @Test
    public void givenGetExchangeRateFromToCurrency_whenNotAvailableToCurrency_thenThrowBadRequestException() {
        
        String fromCurrency = "USD";
        String toCurrency = "GBP";

        ExchangeAPISymbolsDTO symbolsDTO = createSymbolsDTO();
        ExchangeAPIResponseDTO exchangeAPIResponseDTO = createExchangeAPIResponseDTO();

        when(exchangeExternalAPIService.getAcceptedSymbols())
            .thenReturn(symbolsDTO);
        
        when(exchangeExternalAPIService.getExchangeRate(fromCurrency))
            .thenReturn(exchangeAPIResponseDTO);

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> exchangeService.getExchangeRateFromToCurrency(fromCurrency, toCurrency)
        );

        assertTrue(ex.getMessage().equals("Exchange rate not found from currency " + fromCurrency + " to currency " + toCurrency));
    }

    @Test
    public void givenGetExchangeRatesFromCurrency_whenValidInput_thenReturnAllValues() {
        
        String fromCurrency = "USD";

        ExchangeAPISymbolsDTO symbolsDTO = createSymbolsDTO();
        ExchangeAPIResponseDTO exchangeAPIResponseDTO = createExchangeAPIResponseDTO();

        when(exchangeExternalAPIService.getAcceptedSymbols()).thenReturn(symbolsDTO);
        when(exchangeExternalAPIService.getExchangeRate(fromCurrency))
            .thenReturn(exchangeAPIResponseDTO);

        var exchangeRates = exchangeService.getExchangeRatesFromCurrency(fromCurrency);
        assertTrue(exchangeRates.getFromCurrency().equals("USD"));
        assertTrue(exchangeRates.getRates().get("EUR") == 0.85);
        assertTrue(exchangeRates.getRates().get("AED") == 3.672982);
        assertTrue(exchangeRates.getRates().get("AFN") == 57.8936);
    
    }

    @Test
    public void givenGetExchangeRatesFromCurrency_whenInvalidCurrencyProvided_thenThrowBadRequestException() {
        
        String fromCurrencyInvalid = "INVALID";

        ExchangeAPISymbolsDTO symbolsDTO = createSymbolsDTO();
        
        when(exchangeExternalAPIService.getAcceptedSymbols()).thenReturn(symbolsDTO);

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> exchangeService.getExchangeRatesFromCurrency(fromCurrencyInvalid)
        );

        assertTrue(ex.getMessage().equals("Currency " + fromCurrencyInvalid + " is not accepted"));
    }

}
