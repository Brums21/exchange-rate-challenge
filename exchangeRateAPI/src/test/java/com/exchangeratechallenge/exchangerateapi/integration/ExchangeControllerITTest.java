package com.exchangeratechallenge.exchangerateapi.integration;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import io.restassured.RestAssured;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "exchangeAPI=dummy",
        "exchange-url=http://localhost:8081"
    }
)
class ExchangeControllerITTest {

    private WireMockServer wireMockServer;

    private static final String RATE_URL = "http://localhost:%d/api/v1/exchange/rate";
    private static final String RATES_URL = "http://localhost:%d/api/v1/exchange/rates";

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
    void givenGetExchangeRate_whenValidBaseAndTargetCurrency_ReturnCorrectRate() {

        String baseCurrency = "USD";
        String targetCurrency = "EUR";

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
        stubLiveEndpointForExchangeAPI(baseCurrency, MODEL_RESPONSE, 200);

        RestAssured.when()
            .get(String.format(RATE_URL+"?from=%s&to=%s", port, baseCurrency, targetCurrency))
            .then()
            .statusCode(200)
            .body("rate", is(0.85f))
            .body("fromCurrency", is(baseCurrency))
            .body("toCurrency", is(targetCurrency));

    }

    @Test
    void givenGetExchangeRate_whenInvalidBaseOrTargetCurrency_ReturnBadRequest() {

        String baseCurrency = "INVALID";
        String targetCurrency = "BASE";

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);

        RestAssured.when()
            .get(String.format(RATE_URL+"?from=%s&to=%s", port, baseCurrency, targetCurrency))
            .then()
            .statusCode(400);

        baseCurrency = "USD";
        targetCurrency = "INVALID";

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);

        RestAssured.when()
            .get(String.format(RATE_URL+"?from=%s&to=%s", port, baseCurrency, targetCurrency))
            .then()
            .statusCode(400);

    }

    @Test
    void givenGetExchangeRate_whenExchangeAPIReturnsServerError_ReturnInternalServerError() {

        String baseCurrency = "USD";
        String targetCurrency = "EUR";

        stubListEndpointForExchangeAPI("", 502);

        RestAssured.when()
            .get(String.format(RATE_URL+"?from=%s&to=%s", port, baseCurrency, targetCurrency))
            .then()
            .statusCode(502);
    }

    @Test
    void givenGetExchangeRate_whenMissingParameter_ReturnBadRequest() {

        RestAssured.when()
            .get(String.format(RATE_URL+"?from=USD", port))
            .then()
            .statusCode(400)
            .body("error", is("Bad Request"));

        RestAssured.when()
            .get(String.format(RATE_URL+"?to=EUR", port))
            .then()
            .statusCode(400)
            .body("error", is("Bad Request"));
    }

    @Test
    void givenGetExchangeRates_whenValidBaseCurrency_ReturnCorrectRates() {

        String baseCurrency = "USD";

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
        stubLiveEndpointForExchangeAPI(baseCurrency, MODEL_RESPONSE, 200);

        RestAssured.when()
            .get(String.format(RATES_URL+"?from=%s", port, baseCurrency))
            .then()
            .statusCode(200)
            .body("fromCurrency", is(baseCurrency))
            .body("rates.EUR", is(0.85f))
            .body("rates.AED", is(3.672982f))
            .body("rates.AFN", is(57.8936f));
    }

    @Test
    void givenGetExchangeRates_whenExchangeAPIReturnsServerError_ReturnInternalServerError() {

        String baseCurrency = "USD";

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
        stubLiveEndpointForExchangeAPI(baseCurrency, "", 502);

        RestAssured.when()
            .get(String.format(RATES_URL+"?from=%s", port, baseCurrency))
            .then()
            .statusCode(502);
    }

    @Test
    void givenGetExchangeRates_whenMissingParameter_ReturnBadRequest() {

        RestAssured.when()
            .get(String.format(RATES_URL, port))
            .then()
            .statusCode(400)
            .body("error", is("Bad Request"));
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