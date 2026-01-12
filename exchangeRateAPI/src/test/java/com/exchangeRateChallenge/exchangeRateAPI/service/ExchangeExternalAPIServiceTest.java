package com.exchangeRateChallenge.exchangeRateAPI.service;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.exchangeRateChallenge.exchangeRateAPI.exceptions.ExchangeAPIException;
import com.exchangeRateChallenge.exchangeRateAPI.services.ExchangeExternalAPIService;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class ExchangeExternalAPIServiceTest {
    
    private ExchangeExternalAPIService exchangeExternalAPIService;

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

    private static final String symbolsBody = 
        "{\"success\": true,"
        + "\"terms\": \"https://exchangerate.host/terms\","
        + "\"privacy\": \"https://exchangerate.host/privacy\","
        + "\"currencies\": {"
        + "\"USD\": \"United States Dollar\","
        + "\"EUR\": \"Euro\","
        + "\"AED\": \"United Arab Emirates Dirham\","
        + "\"AFN\": \"Afghan Afghani\","
        + "\"GBP\": \"British Pound Sterling\""
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
            
        exchangeExternalAPIService = new ExchangeExternalAPIService(webClient, "test_api_key");
    }

    @Test
    public void givenGetExchangeRate_whenValidInput_thenReturnExchangeRate() {
        
        String fromCurrency = "USD";

        mockWebServer.enqueue(jsonResponse(200, successBody));

        var exchangeDetails = exchangeExternalAPIService.getExchangeRate(fromCurrency);

        assertTrue(exchangeDetails.getSourceCurrency().equals("USD"));
        assertTrue(exchangeDetails.getRates().get("EUR").equals(0.85));
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
            () -> exchangeExternalAPIService.getExchangeRate(fromCurrency)
        );

        assertTrue(ex.getMessage().equals("The provided API key was not provided or is invalid."));
    }

    @Test
    public void givenGetExchangeRate_whenServerErrorOccurs_thenThrowExchangeAPIException() {
        
        String fromCurrency = "USD";

        mockWebServer.enqueue(jsonResponse(502, ""));

        ExchangeAPIException ex = assertThrows(
            ExchangeAPIException.class,
            () -> exchangeExternalAPIService.getExchangeRate(fromCurrency)
        );

        assertTrue(ex.getMessage().equals("Server error 502 BAD_GATEWAY"));

    }
    
    @Test
    public void givenGetExchangeRate_whenClientErrorOccurs_thenThrowExchangeAPIException() {
        
        String fromCurrency = "USD";
        mockWebServer.enqueue(jsonResponse(404, ""));

        ExchangeAPIException ex = assertThrows(
            ExchangeAPIException.class,
            () -> exchangeExternalAPIService.getExchangeRate(fromCurrency)
        );

        assertTrue(ex.getMessage().equals("Client error 404 NOT_FOUND"));

    }
    
    @Test
    public void givenExchangeExternalAPIServiceConstructor_whenApiKeyIsMissing_thenThrowIllegalStateException() {
        
        WebClient webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();

        IllegalStateException exceptionKeyEmpty = assertThrows(
            IllegalStateException.class,
            () -> new ExchangeExternalAPIService(webClient, " ")
        );

        IllegalStateException exceptionKeyNull = assertThrows(
            IllegalStateException.class,
            () -> new ExchangeExternalAPIService(webClient, null)
        );

        String missingAPIMessage = "Missing configuration: exchangeAPI (API key) is not set";

        assertTrue(exceptionKeyEmpty.getMessage().equals(missingAPIMessage));
        assertTrue(exceptionKeyNull.getMessage().equals(missingAPIMessage));

    }
    
    @Test
    public void givenGetListCurrencies_whenValidInput_thenReturnAcceptedSymbols() {
        
        mockWebServer.enqueue(jsonResponse(200, symbolsBody));

        var symbolsDTO = exchangeExternalAPIService.getAcceptedSymbols();

        assertTrue(symbolsDTO.hasSymbol("USD"));
        assertTrue(symbolsDTO.hasSymbol("EUR"));
        assertTrue(symbolsDTO.hasSymbol("GBP"));
    }

    @Test
    public void givenGetListCurrencies_whenInvalidApiKeyProvided_thenThrowExchangeAPIException() {

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
            () -> exchangeExternalAPIService.getAcceptedSymbols()
        );

        assertTrue(ex.getMessage().equals("The provided API key was not provided or is invalid."));
    }

    @Test
    public void givenGetListCurrencies_whenServerErrorOccurs_thenThrowExchangeAPIException() {

        mockWebServer.enqueue(jsonResponse(502, ""));

        ExchangeAPIException ex = assertThrows(
            ExchangeAPIException.class,
            () -> exchangeExternalAPIService.getAcceptedSymbols()
        );

        assertTrue(ex.getMessage().equals("Server error 502 BAD_GATEWAY"));

    }

    private static MockResponse jsonResponse(int code, String body) {
        return new MockResponse()
            .setResponseCode(code)
            .addHeader("Content-Type", "application/json")
            .setBody(body);
    }

}
