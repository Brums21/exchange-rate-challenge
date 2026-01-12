package com.exchangeratechallenge.exchangeRateAPI.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.exchangeratechallenge.exchangeRateAPI.controllers.ExchangeController;
import com.exchangeratechallenge.exchangeRateAPI.exceptions.BadRequestException;
import com.exchangeratechallenge.exchangeRateAPI.exceptions.ExchangeAPIException;
import com.exchangeratechallenge.exchangeRateAPI.models.ExchangeRate;
import com.exchangeratechallenge.exchangeRateAPI.models.ExchangeRates;
import com.exchangeratechallenge.exchangeRateAPI.services.ExchangeService;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;

@WebMvcTest(ExchangeController.class)
public class ExchangeControllerWebMvcTest {

    public static final String RATE_URL = "/api/v1/exchange/rate";
    public static final String RATES_URL = "/api/v1/exchange/rates";
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeService exchangeService;

    @Test
    void givenGetExchangeRateFromAToB_whenValidParametersAreProvided_thenReturnExchangeRate() throws Exception {

        String fromCurrency = "USD";
        String toCurrency = "EUR";
        double mockRate = 0.85;

        ExchangeRate mockRateObj = new ExchangeRate(fromCurrency, toCurrency, mockRate);

        when(exchangeService.getExchangeRateFromToCurrency(fromCurrency, toCurrency)).thenReturn(mockRateObj);

        mockMvc.perform(get(RATE_URL)
                .param("from", fromCurrency)
                .param("to", toCurrency))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(mockRate))
                .andExpect(jsonPath("$.fromCurrency").value(fromCurrency))
                .andExpect(jsonPath("$.toCurrency").value(toCurrency));
    }

    @Test
    void givenGetExchangeRateFromAToB_whenMissingParametersAreProvided_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get(RATE_URL)
                .param("from", "USD"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(RATE_URL)
                .param("to", "EUR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenGetExchangeRateFromAToB_whenFromCurrecyIsInvalid_thenReturnBadRequest() throws Exception {
        String fromCurrency = "INVALID";
        String toCurrency = "EUR";

        when(exchangeService.getExchangeRateFromToCurrency(fromCurrency, toCurrency))
            .thenThrow(new BadRequestException("Exchange rate not found from currency " + fromCurrency + " to currency " + toCurrency));

        mockMvc.perform(get(RATE_URL)
                .param("from", fromCurrency)
                .param("to", toCurrency))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenGetExchangeRateFromAToB_whenExchangeAPIReturnsServerError_thenReturnInternalServerError() throws Exception {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR";

        when(exchangeService.getExchangeRateFromToCurrency(fromCurrency, toCurrency))
            .thenThrow(new ExchangeAPIException("Server error: 502 BAD_GATEWAY"));

        mockMvc.perform(get(RATE_URL)
                .param("from", fromCurrency)
                .param("to", toCurrency))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void givenGetExchangeRateFromA_whenValidParametersAreProvided_thenReturnExchangeRate() throws Exception {

        String fromCurrency = "USD";
        Map<String, Double> rates = new HashMap<>();
        rates.put("EUR", 0.85);
        rates.put("GBP", 0.75);

        ExchangeRates mockRateObj = new ExchangeRates(fromCurrency, rates);

        when(exchangeService.getExchangeRatesFromCurrency(fromCurrency)).thenReturn(mockRateObj);

        mockMvc.perform(get(RATES_URL)
                .param("from", fromCurrency))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rates").value(rates))
                .andExpect(jsonPath("$.fromCurrency").value(fromCurrency));
    }

}