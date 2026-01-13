package com.exchangeratechallenge.exchangeRateAPI.integration;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.github.tomakehurst.wiremock.WireMockServer;

import io.restassured.RestAssured;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "exchangeAPI=dummy",
        "exchange-url=http://localhost:8081"
    }
)
public class ConversionControllerITTest {
    private WireMockServer wireMockServer;

    private static final String CONVERSION_URL = "http://localhost:%d/api/v1/convert/currency";

    private static final String MODEL_RESPONSE = "{"
        + "\"success\": true,"
        + "\"terms\": \"https://exchangerate.host/terms\","
        + "\"privacy\": \"https://exchangerate.host/privacy\","
        + "\"timestamp\": 1430401802,"
        + "\"source\": \"USD\","
        + "\"quotes\": {"
        + "\"USDAED\": 3.672982,"
        + "\"USDAFN\": 57.8936,"
        + "\"USDEUR\": 0.85"
        + "}}";

    private static final String SYMBOLS_MODEL_RESPONSE = "{"
        + "\"success\": true,"
        + "\"terms\": \"https://exchangerate.host/terms\","
        + "\"privacy\": \"https://exchangerate.host/privacy\","
        + "\"currencies\": {"
        + "\"USD\": \"United States Dollar\","
        + "\"EUR\": \"Euro\","
        + "\"AED\": \"United Arab Emirates Dirham\","
        + "\"AFN\": \"Afghan Afghani\""
        + "}}";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();
        configureFor("localhost", 8081);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void givenValidConversionRequest_whenGetConversion_thenReturnConversionResponse() {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR,AFN";
        Double value = 10.0;

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
        stubLiveEndpointForExchangeAPI(fromCurrency, MODEL_RESPONSE, 200);

        RestAssured.given()
            .param("from", fromCurrency)
            .param("to", toCurrency)
            .param("value", value)
            .when()
            .get(String.format(CONVERSION_URL, port))
            .then()
            .statusCode(200)
            .body("from", is("USD"))
            .body("originalValue", is(10.0))
            .body("conversion.EUR", is(8.5f))
            .body("conversion.AFN", is(578.936f));
    }

    @Test
    void givenInvalidConversionRequest_whenGetConversion_thenReturnBadRequest() {
        
        String fromCurrency = "INVALID";
        String toCurrency = "EUR,AFN";
        Double value = 10.0;

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
        stubLiveEndpointForExchangeAPI(fromCurrency, MODEL_RESPONSE, 200);

        RestAssured.given()
            .param("from", fromCurrency)
            .param("to", toCurrency)
            .param("value", value)
            .when()
            .get(String.format(CONVERSION_URL, port))
            .then()
            .statusCode(400);
    }

    @Test
    void givenMissingParameters_whenGetConversion_thenReturnBadRequest() {
        
        RestAssured.given()
            .param("from", "USD")
            .param("value", "100")
            .when()
            .get(String.format(CONVERSION_URL, port))
            .then()
            .statusCode(400);

        RestAssured.given()
            .param("to", "EUR")
            .param("value", "100")
            .when()
            .get(String.format(CONVERSION_URL, port))
            .then()
            .statusCode(400);

        RestAssured.given()
            .param("from", "USD")
            .param("to", "EUR")
            .when()
            .get(String.format(CONVERSION_URL, port))
            .then()
            .statusCode(400);
    }

    @Test
    void givenExchangeAPIServerError_whenGetConversion_thenReturnInternalServerError() {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR,AFN";
        Double value = 10.0;

        stubListEndpointForExchangeAPI("", 502);

        RestAssured.given()
            .param("from", fromCurrency)
            .param("to", toCurrency)
            .param("value", value)
            .when()
            .get(String.format(CONVERSION_URL, port))
            .then()
            .statusCode(502);
    }

    @Test
    void givenNegativeValue_whenGetConversion_thenReturnBadRequest() {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR,AFN";
        Double value = -10.0;

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
        stubLiveEndpointForExchangeAPI(fromCurrency, MODEL_RESPONSE, 200);

        RestAssured.given()
            .param("from", fromCurrency)
            .param("to", toCurrency)
            .param("value", value)
            .when()
            .get(String.format(CONVERSION_URL, port))
            .then()
            .statusCode(400);
    }

    @Test
    void givenNonNumericValue_whenGetConversion_thenReturnBadRequest() {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR,AFN";
        String value = "invalid";

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
        stubLiveEndpointForExchangeAPI(fromCurrency, MODEL_RESPONSE, 200);

        RestAssured.given()
            .param("from", fromCurrency)
            .param("to", toCurrency)
            .param("value", value)
            .when()
            .get(String.format(CONVERSION_URL, port))
            .then()
            .statusCode(400);
    }

    @Test
    void givenInvalidToCurrency_whenGetConversion_thenReturnBadRequest() {
        
        String fromCurrency = "USD";
        String toCurrency = "EUR,INVALID";
        Double value = 10.0;

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
        stubLiveEndpointForExchangeAPI(fromCurrency, MODEL_RESPONSE, 200);

        RestAssured.given()
            .param("from", fromCurrency)
            .param("to", toCurrency)
            .param("value", value)
            .when()
            .get(String.format(CONVERSION_URL, port))
            .then()
            .statusCode(400);
    }

    private void stubLiveEndpointForExchangeAPI(String baseCurrency, String responseBody, int code) {
        stubFor(get(urlPathEqualTo("/live"))
            .withQueryParam("access_key", equalTo("dummy"))
            .withQueryParam("source", equalTo(baseCurrency))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)
                .withStatus(code)));
    }

    private void stubListEndpointForExchangeAPI(String responseBody, int code) {
        stubFor(get(urlPathEqualTo("/list"))
            .withQueryParam("access_key", equalTo("dummy"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)
                .withStatus(code)));
    }
    
}
