package com.exchangeRateChallenge.exchangeRateAPI.integration;

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
public class ExchangeControllerITTest {

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
    public void givenGetExchangeRate_whenValidBaseAndTargetCurrency_ReturnCorrectRate() {

        String baseCurrency = "USD";
        String targetCurrency = "EUR";

        stubWireMockForExchangeAPI(baseCurrency, MODEL_RESPONSE, 200);

        RestAssured.when()
            .get(String.format(RATE_URL+"?from=%s&to=%s", port, baseCurrency, targetCurrency))
            .then()
            .statusCode(200)
            .body("rate", is(0.85f))
            .body("fromCurrency", is(baseCurrency))
            .body("toCurrency", is(targetCurrency));

    }

    @Test
    public void givenGetExchangeRate_whenInvalidTargetCurrency_ReturnBadRequest() {

        String baseCurrency = "USD";
        String targetCurrency = "INVALID";

        stubWireMockForExchangeAPI(baseCurrency, MODEL_RESPONSE, 200);

        RestAssured.when()
            .get(String.format(RATE_URL+"?from=%s&to=%s", port, baseCurrency, targetCurrency))
            .then()
            .statusCode(400);
    }

    @Test
    public void givenGetExchangeRate_whenExchangeAPIReturnsServerError_ReturnInternalServerError() {

        String baseCurrency = "USD";
        String targetCurrency = "EUR";

        stubWireMockForExchangeAPI(baseCurrency, "", 502);

        RestAssured.when()
            .get(String.format(RATE_URL+"?from=%s&to=%s", port, baseCurrency, targetCurrency))
            .then()
            .statusCode(502);
    }

    @Test
    public void givenGetExchangeRate_whenMissingParameter_ReturnBadRequest() {

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
    public void givenGetExchangeRates_whenValidBaseCurrency_ReturnCorrectRates() {

        String baseCurrency = "USD";

        stubWireMockForExchangeAPI(baseCurrency, MODEL_RESPONSE, 200);

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
    public void givenGetExchangeRates_whenExchangeAPIReturnsServerError_ReturnInternalServerError() {

        String baseCurrency = "USD";

        stubWireMockForExchangeAPI(baseCurrency, "", 502);


        RestAssured.when()
            .get(String.format(RATES_URL+"?from=%s", port, baseCurrency))
            .then()
            .statusCode(502);
    }

    @Test
    public void givenGetExchangeRates_whenMissingParameter_ReturnBadRequest() {

        RestAssured.when()
            .get(String.format(RATES_URL, port))
            .then()
            .statusCode(400)
            .body("error", is("Bad Request"));
    }

    private void stubWireMockForExchangeAPI(String baseCurrency, String responseBody, int code) {
        stubFor(get(urlPathEqualTo("/live"))
            .withQueryParam("access_key", equalTo("dummy"))
            .withQueryParam("source", equalTo(baseCurrency))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)
                .withStatus(code)));
    }

}