package com.exchangeRateChallenge.exchangeRateAPI.service;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.exchangeRateChallenge.exchangeRateAPI.exceptions.BadRequestException;
import com.exchangeRateChallenge.exchangeRateAPI.exceptions.ExchangeAPIException;
import com.exchangeRateChallenge.exchangeRateAPI.models.ExchangeRate;
import com.exchangeRateChallenge.exchangeRateAPI.services.ExchangeAPIService;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class ExchangeAPIServiceTest {
    
    private ExchangeAPIService exchangeAPIService;

    private static MockWebServer mockWebServer;

    private static final String successBody = 
        "{\"success\": true,"
        + "\"terms\": \"https://exchangerate.host/terms\","
        + "\"privacy\": \"https://exchangerate.host/privacy\","
        + "\"timestamp\": 1430401802,"
        + "\"source\": \"USD\","
        + "\"quotes\": {"
        + "\"USDAED\": 3.672982,"
        + "\"USDAFN\": 57.8936,"
        + "\"USDEUR\": 0.85"
        + "}}";

    @BeforeAll
    static void setUp() throws IOException{
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        WebClient webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();
            
        exchangeAPIService = new ExchangeAPIService(webClient, "test_api_key");
    }

    @Test
    public void givenGetExchangeRateFromToCurrency_whenValidInput_then() {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        
        mockWebServer.enqueue(jsonResponse(200, successBody));

        ExchangeRate exchangeCurrent = exchangeAPIService.getExchangeRateFromToCurrency(fromCurrency, toCurrency);
        assertTrue(exchangeCurrent.getRate() == 0.85);
        assertTrue(exchangeCurrent.getFromCurrency().equals("USD"));
        assertTrue(exchangeCurrent.getToCurrency().equals("EUR"));
    
    }

    @Test
    public void givenGetExchangeRateFromToCurrency_whenInvalidToCurrencyProvided_thenThrowBadRequestException() {
        
        String fromCurrency = "USD";
        String toCurrency = "INVALID";

        mockWebServer.enqueue(jsonResponse(200, successBody));

        BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> exchangeAPIService.getExchangeRateFromToCurrency(fromCurrency, toCurrency)
        );

        assertTrue(ex.getMessage().equals("Exchange rate not found from currency " + fromCurrency + " to currency " + toCurrency));
    }

    @Test
    public void givenGetExchangeRate_whenInvalidApiKeyProvided_thenThrowExchangeAPIException() {
        
        String fromCurrency = "USD";

        mockWebServer.enqueue(jsonResponse(
            401,
            "{\"success\": false,"
                + "\"error\": {"
                + "\"code\": 101,"
                + "\"type\": \"invalid_access_key\","
                + "\"info\": \"You have not supplied a valid API Access Key.\""
                + "}}"
        ));

        ExchangeAPIException ex = assertThrows(
            ExchangeAPIException.class,
            () -> exchangeAPIService.getExchangeRate(fromCurrency)
        );

        assertTrue(ex.getMessage().equals("The provided API key was not provided or is invalid."));
    }

    @Test
    public void givenGetExchangeRate_whenServerErrorOccurs_thenThrowExchangeAPIException() {
        
        String fromCurrency = "USD";

        mockWebServer.enqueue(jsonResponse(502, ""));

        ExchangeAPIException ex = assertThrows(
            ExchangeAPIException.class,
            () -> exchangeAPIService.getExchangeRate(fromCurrency)
        );

        assertTrue(ex.getMessage().equals("Server error 502 BAD_GATEWAY"));

    }

    @Test
    public void givenGetExchangeRate_when4xxErrorOccurs_thenThrowExchangeAPIException() {
        
        String fromCurrency = "USD";
        mockWebServer.enqueue(jsonResponse(404, ""));

        ExchangeAPIException ex = assertThrows(
            ExchangeAPIException.class,
            () -> exchangeAPIService.getExchangeRate(fromCurrency)
        );

        assertTrue(ex.getMessage().equals("Client error 404 NOT_FOUND"));

    }

    @Test
    public void givenExchangeAPIServiceConstructor_whenApiKeyIsMissing_thenThrowIllegalStateException() {
        WebClient webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();

        IllegalStateException exceptionKeyEmpty = assertThrows(
            IllegalStateException.class,
            () -> new ExchangeAPIService(webClient, " ")
        );

        IllegalStateException exceptionKeyNull = assertThrows(
            IllegalStateException.class,
            () -> new ExchangeAPIService(webClient, null)
        );

        String missingAPIMessage = "Missing configuration: exchangeAPI (API key) is not set";

        assertTrue(exceptionKeyEmpty.getMessage().equals(missingAPIMessage));
        assertTrue(exceptionKeyNull.getMessage().equals(missingAPIMessage));

    }

    @Test
    public void givenGetExchangeRatesFromCurrency_whenValidInput_thenReturnAllValues() {
        
        String fromCurrency = "USD";
        
        mockWebServer.enqueue(jsonResponse(200, successBody));

        var exchangeRates = exchangeAPIService.getExchangeRatesFromCurrency(fromCurrency);
        assertTrue(exchangeRates.getFromCurrency().equals("USD"));
        assertTrue(exchangeRates.getRates().get("EUR") == 0.85);
        assertTrue(exchangeRates.getRates().get("AED") == 3.672982);
        assertTrue(exchangeRates.getRates().get("AFN") == 57.8936);
    
    }

    private static MockResponse jsonResponse(int code, String body) {
        return new MockResponse()
            .setResponseCode(code)
            .addHeader("Content-Type", "application/json")
            .setBody(body);
    }

}
