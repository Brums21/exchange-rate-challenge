package com.exchangeratechallenge.exchangerateapi.service;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.exchangeratechallenge.exchangerateapi.exceptions.ExchangeAPIException;
import com.exchangeratechallenge.exchangerateapi.services.ExchangeExternalAPIService;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class ExchangeExternalAPIServiceTest {
    
    private ExchangeExternalAPIService exchangeExternalAPIService;

    private static MockWebServer mockWebServer;

    private static final String SUCCESS_BODY = 
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

    private static final String SYMBOLS_BODY = 
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
    void givenGetExchangeRate_whenValidInput_thenReturnExchangeRate() {
        
        String fromCurrency = "USD";

        mockWebServer.enqueue(jsonResponse(200, SUCCESS_BODY));

        var exchangeDetails = exchangeExternalAPIService.getExchangeRate(fromCurrency);

        assertEquals("USD", exchangeDetails.getSourceCurrency());
        assertEquals(0.85, exchangeDetails.getRates().get("EUR"));
    }

    @Test
    void givenGetExchangeRate_whenInvalidApiKeyProvided_thenThrowExchangeAPIException() {
        
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

        assertEquals("The provided API key was not provided or is invalid.", ex.getMessage());
    }

    @Test
    void givenGetExchangeRate_whenServerErrorOccurs_thenThrowExchangeAPIException() {
        
        String fromCurrency = "USD";

        mockWebServer.enqueue(jsonResponse(502, ""));

        ExchangeAPIException ex = assertThrows(
            ExchangeAPIException.class,
            () -> exchangeExternalAPIService.getExchangeRate(fromCurrency)
        );

        assertEquals("Server error 502 BAD_GATEWAY", ex.getMessage());

    }
    
    @Test
    void givenGetExchangeRate_whenClientErrorOccurs_thenThrowExchangeAPIException() {
        
        String fromCurrency = "USD";
        mockWebServer.enqueue(jsonResponse(404, ""));

        ExchangeAPIException ex = assertThrows(
            ExchangeAPIException.class,
            () -> exchangeExternalAPIService.getExchangeRate(fromCurrency)
        );

        assertEquals("Client error 404 NOT_FOUND", ex.getMessage());

    }
    
    @Test
    void givenExchangeExternalAPIServiceConstructor_whenApiKeyIsMissing_thenThrowIllegalStateException() {
        
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

        assertEquals(exceptionKeyEmpty.getMessage(), missingAPIMessage);
        assertEquals(exceptionKeyNull.getMessage(), missingAPIMessage);

    }
    
    @Test
    void givenGetListCurrencies_whenValidInput_thenReturnAcceptedSymbols() {
        
        mockWebServer.enqueue(jsonResponse(200, SYMBOLS_BODY));

        var symbolsDTO = exchangeExternalAPIService.getAcceptedSymbols();

        assertTrue(symbolsDTO.hasSymbol("USD"));
        assertTrue(symbolsDTO.hasSymbol("EUR"));
        assertTrue(symbolsDTO.hasSymbol("GBP"));
    }

    @Test
    void givenGetListCurrencies_whenInvalidApiKeyProvided_thenThrowExchangeAPIException() {

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

        assertEquals("The provided API key was not provided or is invalid.", ex.getMessage());
    }

    @Test
    void givenGetListCurrencies_whenServerErrorOccurs_thenThrowExchangeAPIException() {

        mockWebServer.enqueue(jsonResponse(502, ""));

        ExchangeAPIException ex = assertThrows(
            ExchangeAPIException.class,
            () -> exchangeExternalAPIService.getAcceptedSymbols()
        );

        assertEquals("Server error 502 BAD_GATEWAY", ex.getMessage());

    }

    private static MockResponse jsonResponse(int code, String body) {
        return new MockResponse()
            .setResponseCode(code)
            .addHeader("Content-Type", "application/json")
            .setBody(body);
    }

}
