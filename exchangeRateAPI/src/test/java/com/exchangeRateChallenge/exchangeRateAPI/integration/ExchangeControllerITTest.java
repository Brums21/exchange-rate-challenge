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

    private static final String BASE_URL = "http://localhost:%d/api/v1/exchange/exchange-rate";

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

        stubFor(get(urlPathEqualTo("/live"))
            .withQueryParam("access_key", equalTo("dummy"))
            .withQueryParam("source", equalTo(baseCurrency))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"success\": true,"
                + "\"terms\": \"https://exchangerate.host/terms\","
                + "\"privacy\": \"https://exchangerate.host/privacy\","
                + "\"timestamp\": 1430401802,"
                + "\"source\": \"USD\","
                + "\"quotes\": {"
                + "\"USDAED\": 3.672982,"
                + "\"USDAFN\": 57.8936,"
                + "\"USDEUR\": 0.85"
                + "}}")));

        RestAssured.when()
            .get(String.format(BASE_URL+"?from=%s&to=%s", port, baseCurrency, targetCurrency))
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

        stubFor(get(urlPathEqualTo("/live"))
            .withQueryParam("access_key", equalTo("dummy"))
            .withQueryParam("source", equalTo(baseCurrency))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"success\": true,"
                + "\"terms\": \"https://exchangerate.host/terms\","
                + "\"privacy\": \"https://exchangerate.host/privacy\","
                + "\"timestamp\": 1430401802,"
                + "\"source\": \"USD\","
                + "\"quotes\": {"
                + "\"USDAED\": 3.672982,"
                + "\"USDAFN\": 57.8936,"
                + "\"USDEUR\": 0.85"
                + "}}")));

        RestAssured.when()
            .get(String.format(BASE_URL+"?from=%s&to=%s", port, baseCurrency, targetCurrency))
            .then()
            .statusCode(400);
    }

    @Test
    public void givenGetExchangeRate_whenExchangeAPIReturnsServerError_ReturnInternalServerError() {

        String baseCurrency = "USD";
        String targetCurrency = "EUR";

        stubFor(get(urlPathEqualTo("/live"))
            .withQueryParam("access_key", equalTo("dummy"))
            .withQueryParam("source", equalTo(baseCurrency))
            .willReturn(aResponse()
                .withStatus(502)));

        RestAssured.when()
            .get(String.format(BASE_URL+"?from=%s&to=%s", port, baseCurrency, targetCurrency))
            .then()
            .statusCode(502);
    }

    @Test
    public void givenGetExchangeRate_whenMissingParameters_ReturnBadRequest() {

        RestAssured.when()
            .get(String.format(BASE_URL+"?from=USD", port))
            .then()
            .statusCode(400)
            .body("error", is("Bad Request"));

        RestAssured.when()
            .get(String.format(BASE_URL+"?to=EUR", port))
            .then()
            .statusCode(400)
            .body("error", is("Bad Request"));
    }
}