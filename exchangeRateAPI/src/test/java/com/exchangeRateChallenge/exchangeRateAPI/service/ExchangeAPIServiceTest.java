package com.exchangeRateChallenge.exchangeRateAPI.service;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.exchangeRateChallenge.exchangeRateAPI.exceptions.BadRequestException;
import com.exchangeRateChallenge.exchangeRateAPI.exceptions.ExchangeAPIException;
import com.exchangeRateChallenge.exchangeRateAPI.models.ExchangeCurrency;
import com.exchangeRateChallenge.exchangeRateAPI.services.ExchangeAPIService;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@ExtendWith(MockitoExtension.class)
public class ExchangeAPIServiceTest {
    
    private ExchangeAPIService exchangeAPIService;

    private static MockWebServer mockWebServer;

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
        
        mockWebServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(
                "{\"success\": true,"
                + "\"terms\": \"https://exchangerate.host/terms\","
                + "\"privacy\": \"https://exchangerate.host/privacy\","
                + "\"timestamp\": 1430401802,"
                + "\"source\": \"USD\","
                + "\"quotes\": {"
                + "\"USDAED\": 3.672982,"
                + "\"USDAFN\": 57.8936,"
                + "\"USDEUR\": 0.85"
                + "}}"
            )
        );

        ExchangeCurrency exchangeCurrent = exchangeAPIService.getExchangeRateFromToCurrency(fromCurrency, toCurrency);
        assertTrue(exchangeCurrent.getRate() == 0.85);
        assertTrue(exchangeCurrent.getFromCurrency().equals("USD"));
        assertTrue(exchangeCurrent.getToCurrency().equals("EUR"));
    
    }

    @Test
    public void givenGetExchangeRateFromToCurrency_whenInvalidToCurrencyProvided_thenThrowBadRequestException() {
        
        String fromCurrency = "USD";
        String toCurrency = "INVALID";

        mockWebServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(
                "{\"success\": true,"
                + "\"terms\": \"https://exchangerate.host/terms\","
                + "\"privacy\": \"https://exchangerate.host/privacy\","
                + "\"timestamp\": 1430401802,"
                + "\"source\": \"USD\","
                + "\"quotes\": {"
                + "\"USDAED\": 3.672982,"
                + "\"USDAFN\": 57.8936,"
                + "\"USDEUR\": 0.85"
                + "}}"
            )
        );

        try {
            exchangeAPIService.getExchangeRateFromToCurrency(fromCurrency, toCurrency);
        } catch (Exception e) {
            assertTrue(e instanceof BadRequestException);
            assertTrue(e.getMessage().equals("Exchange rate not found from currency " + fromCurrency + " to currency " + toCurrency));
        }
    }

    @Test
    public void givenGetExchangeRate_whenInvalidApiKeyProvided_thenThrowExchangeAPIException() {
        
        String fromCurrency = "USD";

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(401)
            .addHeader("Content-Type", "application/json")
            .setBody(
                "{\"success\": false,"
                + "\"error\": {"
                + "\"code\": 101,"
                + "\"type\": \"invalid_access_key\","
                + "\"info\": \"You have not supplied a valid API Access Key.\""
                + "}}"
            )
        );

        try {
            exchangeAPIService.getExchangeRate(fromCurrency);
        } catch (Exception e) {
            assertTrue(e instanceof ExchangeAPIException);
            assertTrue(e.getMessage().equals("The provided API key was not provided or is invalid."));
        }
    }

    @Test
    public void givenGetExchangeRate_whenServerErrorOccurs_thenThrowExchangeAPIException() {
        
        String fromCurrency = "USD";

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(502)
            .addHeader("Content-Type", "application/json")
        );

        try {
            exchangeAPIService.getExchangeRate(fromCurrency);
        } catch (Exception e) {
            assertTrue(e instanceof ExchangeAPIException);
            assertTrue(e.getMessage().equals("Server error 502 BAD_GATEWAY"));
        }
    }

    @Test
    public void givenGetExchangeRate_when4xxErrorOccurs_thenThrowExchangeAPIException() {
        
        String fromCurrency = "USD";

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .addHeader("Content-Type", "application/json")
        );

        try {
            exchangeAPIService.getExchangeRate(fromCurrency);
        } catch (Exception e) {
            assertTrue(e instanceof ExchangeAPIException);
            assertTrue(e.getMessage().equals("Client error 404 NOT_FOUND"));
        }
    }

    @Test
    public void givenExchangeAPIService_whenApiKeyIsMissing_thenThrowIllegalStateException() {
        WebClient webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();
        
        try {
            new ExchangeAPIService(webClient, "   ");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
            assertTrue(e.getMessage().equals("Missing configuration: exchangeAPI (API key) is not set"));
        }

        try {
            new ExchangeAPIService(webClient, null);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
            assertTrue(e.getMessage().equals("Missing configuration: exchangeAPI (API key) is not set"));
        }
    }

}
