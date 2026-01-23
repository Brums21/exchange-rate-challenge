package com.exchangeratechallenge.exchangerateapi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "exchangeAPI=dummy",
        "exchange-url=http://localhost:8081"
    }
)
@AutoConfigureMockMvc
@TestPropertySource(properties = "rate-limiter.enabled=true")
public class ThrottlingFilterITTest {

    @Autowired
    CacheManager cacheManager;

    private WireMockServer wireMockServer;

    public static final String RATES_URL = "/api/v1/exchange/rates";

    @Autowired 
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();

        cacheManager.getCache("symbols").clear();
        cacheManager.getCache("exchangeRates").clear();
    }

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

    @Test
    void givenUsersGetAPIMethod_whenGetTooManyRequests_thenReturnTooManyRequests() throws Exception {

        for (int i = 0; i < 5; i++) {
            stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
            stubLiveEndpointForExchangeAPI("USD", MODEL_RESPONSE, 200);
            mockMvc.perform(get(RATES_URL)
                    .param("from", "USD")
                    .with(req -> { req.setRemoteAddr("1.1.1.1"); return req; }))
                .andExpect(status().is2xxSuccessful());
        }

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
        stubLiveEndpointForExchangeAPI("USD", MODEL_RESPONSE, 200);

        mockMvc.perform(get(RATES_URL)
                .param("from", "USD")
                .with(req -> { req.setRemoteAddr("1.1.1.1"); return req; }))
            .andExpect(status().isTooManyRequests());

        stubListEndpointForExchangeAPI(SYMBOLS_MODEL_RESPONSE, 200);
        stubLiveEndpointForExchangeAPI("USD", MODEL_RESPONSE, 200);

        mockMvc.perform(get(RATES_URL)
                .param("from", "USD")
                .with(req -> { req.setRemoteAddr("2.2.2.2"); return req; }))
            .andExpect(status().is2xxSuccessful());
    }

    private void stubLiveEndpointForExchangeAPI(String baseCurrency, String responseBody, int code) {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/live"))
            .withQueryParam("access_key", WireMock.equalTo("dummy"))
            .withQueryParam("source", WireMock.equalTo(baseCurrency))
            .willReturn(WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)
                .withStatus(code)));
    }

    private void stubListEndpointForExchangeAPI(String responseBody, int code) {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/list"))
            .withQueryParam("access_key", WireMock.equalTo("dummy"))
            .willReturn(WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)
                .withStatus(code)));
    }

}
