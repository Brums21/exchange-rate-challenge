package com.exchangeratechallenge.exchangerateapi.controller;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.exchangeratechallenge.exchangerateapi.controllers.ConversionController;
import com.exchangeratechallenge.exchangerateapi.exceptions.BadRequestException;
import com.exchangeratechallenge.exchangerateapi.exceptions.ExchangeAPIException;
import com.exchangeratechallenge.exchangerateapi.models.Conversion;
import com.exchangeratechallenge.exchangerateapi.services.ConversionService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ConversionController.class)
public class ConversionControllerWebMvcTest {
    public static final String CONVERSION_URL = "/api/v1/convert/currency";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    ConversionService conversionService;

    @Test
    void givenGetConversions_whenValidParametersAreProvided_thenReturnConversions() throws Exception {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR,GBP,JPY";
        String value = "100";

        Conversion mockedConversion = new Conversion();
        mockedConversion.setFromCurrency(fromCurrency);
        mockedConversion.setOriginalValue(100.0);
        mockedConversion.setConvertedCurrencies(Map.of(
            "EUR", 85.0,
            "GBP", 75.0,
            "JPY", 11000.0
        ));

        when(conversionService.getConversionValues(fromCurrency, toCurrency, 100.0)).thenReturn(mockedConversion);

        mockMvc.perform(get(CONVERSION_URL)
                .param("from", fromCurrency)
                .param("to", toCurrency)
                .param("value", value))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCurrency").value(fromCurrency))
                .andExpect(jsonPath("$.convertedCurrencies.EUR").value(85.0))
                .andExpect(jsonPath("$.convertedCurrencies.GBP").value(75.0))
                .andExpect(jsonPath("$.convertedCurrencies.JPY").value(11000.0));
    }

    @Test
    void givenGetConversions_whenMissingParametersAreProvided_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get(CONVERSION_URL)
                .param("from", "USD")
                .param("value", "100"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(CONVERSION_URL)
                .param("to", "EUR")
                .param("value", "100"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(CONVERSION_URL)
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenGetConversions_whenFromCurrencyIsInvalid_thenReturnBadRequest() throws Exception {
        String fromCurrency = "INVALID";
        String toCurrency = "EUR";
        String value = "100";

        when(conversionService.getConversionValues(fromCurrency, toCurrency, 100.0))
            .thenThrow(new BadRequestException("Conversion not found from currency INVALID"));

        mockMvc.perform(get(CONVERSION_URL)
                .param("from", fromCurrency)
                .param("to", toCurrency)
                .param("value", value))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenGetConversions_whenExchangeAPIReturnsServerError_thenReturnInternalServerError() throws Exception {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String value = "100";

        when(conversionService.getConversionValues(fromCurrency, toCurrency, 100.0))
            .thenThrow(new ExchangeAPIException("Server error: 502 BAD_GATEWAY"));

        mockMvc.perform(get(CONVERSION_URL)
                .param("from", fromCurrency)
                .param("to", toCurrency)
                .param("value", value))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void givenGetConversions_whenToCurrencyIsInvalid_thenReturnBadRequest() throws Exception {
        String fromCurrency = "USD";
        String toCurrency = "EUR,INVALID";
        String value = "100";

        when(conversionService.getConversionValues(fromCurrency, toCurrency, 100.0))
            .thenThrow(new BadRequestException("Conversion not found for currency INVALID"));

        mockMvc.perform(get(CONVERSION_URL)
                .param("from", fromCurrency)
                .param("to", toCurrency)
                .param("value", value))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenGetConversions_whenValueIsInvalid_thenReturnBadRequest() throws Exception {
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        String value = "INVALID";

        mockMvc.perform(get(CONVERSION_URL)
                .param("from", fromCurrency)
                .param("to", toCurrency)
                .param("value", value))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenGetConversions_whenValueIsNegative_thenReturnBadRequest() throws Exception {
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        Double value = -100.0;

        mockMvc.perform(get(CONVERSION_URL)
                .param("from", fromCurrency)
                .param("to", toCurrency)
                .param("value", value.toString()))
                .andExpect(status().isBadRequest());
    }

}
