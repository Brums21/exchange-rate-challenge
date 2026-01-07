package com.exchangeRateChallenge.exchangeRateAPI.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.exchangeRateChallenge.exchangeRateAPI.controllers.ExchangeController;
import com.exchangeRateChallenge.exchangeRateAPI.exceptions.BadRequestException;
import com.exchangeRateChallenge.exchangeRateAPI.exceptions.ExchangeAPIException;
import com.exchangeRateChallenge.exchangeRateAPI.models.ExchangeCurrency;
import com.exchangeRateChallenge.exchangeRateAPI.services.ExchangeAPIService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

@WebMvcTest(ExchangeController.class)
public class ExchangeControllerWebMvcTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeAPIService exchangeService;

    @Test
    public void givenGetExchangeRate_whenValidParametersAreProvided_thenReturnExchangeRate() throws Exception {

        String fromCurrency = "USD";
        String toCurrency = "EUR";
        double mockRate = 0.85;

        ExchangeCurrency mockRateObj = new ExchangeCurrency(fromCurrency, toCurrency, mockRate);

        when(exchangeService.getExchangeRateFromToCurrency(fromCurrency, toCurrency)).thenReturn(mockRateObj);

        mockMvc.perform(get("/api/v1/exchange/exchange-rate")
                .param("from", fromCurrency)
                .param("to", toCurrency))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(mockRate))
                .andExpect(jsonPath("$.fromCurrency").value(fromCurrency))
                .andExpect(jsonPath("$.toCurrency").value(toCurrency));
    }

    @Test
    public void givenGetExchangeRate_whenMissingParametersAreProvided_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/exchange/exchange-rate")
                .param("from", "USD"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/exchange/exchange-rate")
                .param("to", "EUR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenGetExchangeRate_whenFromCurrecyIsInvalid_thenReturnBadRequest() throws Exception {
        String fromCurrency = "INVALID";
        String toCurrency = "EUR";

        when(exchangeService.getExchangeRateFromToCurrency(fromCurrency, toCurrency))
            .thenThrow(new BadRequestException("Exchange rate not found from currency " + fromCurrency + " to currency " + toCurrency));

        mockMvc.perform(get("/api/v1/exchange/exchange-rate")
                .param("from", fromCurrency)
                .param("to", toCurrency))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenGetExchangeRate_whenExchangeAPIReturnsServerError_thenReturnInternalServerError() throws Exception {
        String fromCurrency = "USD";
        String toCurrency = "EUR";

        when(exchangeService.getExchangeRateFromToCurrency(fromCurrency, toCurrency))
            .thenThrow(new ExchangeAPIException("Server error: 502 BAD_GATEWAY"));

        mockMvc.perform(get("/api/v1/exchange/exchange-rate")
                .param("from", fromCurrency)
                .param("to", toCurrency))
                .andExpect(status().is5xxServerError());
    }

}