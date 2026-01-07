package com.exchangeRateChallenge.exchangeRateAPI.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.exchangeRateChallenge.exchangeRateAPI.exceptions.BadRequestException;
import com.exchangeRateChallenge.exchangeRateAPI.exceptions.ExchangeAPIException;
import com.exchangeRateChallenge.exchangeRateAPI.models.ExchangeCurrency;
import com.exchangeRateChallenge.exchangeRateAPI.models.DTOs.ExchangeDetailsDTO;

import reactor.core.publisher.Mono;

@Service
public class ExchangeAPIService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeAPIService.class);

    private final WebClient webClient;
    private final String apiKey;

    public ExchangeAPIService(WebClient webClient, @Value("${exchangeAPI}") String apiKey) {
        this.webClient = webClient;
        this.apiKey = apiKey;

        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalStateException("Missing configuration: exchangeAPI (API key) is not set");
        }
    }

    public ExchangeCurrency getExchangeRateFromToCurrency(String fromCurrency, String toCurrency) {

        LOGGER.info("Fetching exchange rate from {} to {}", fromCurrency, toCurrency);

        ExchangeDetailsDTO exchangeDetailsDTO = getExchangeRate(fromCurrency);
        Double rate = exchangeDetailsDTO.getRates().get(toCurrency);

        if (rate == null) {
            throw new BadRequestException("Exchange rate not found from currency " + fromCurrency + " to currency " + toCurrency);
        }

        return new ExchangeCurrency(fromCurrency, toCurrency, rate);
    }
    
    public ExchangeDetailsDTO getExchangeRate(String fromCurrency) {

        LOGGER.info("Fetching exchange rate from {}", fromCurrency);

        ExchangeDetailsDTO exchangeDetailsDTO = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/live")
                .queryParam("access_key", apiKey.trim())
                .queryParam("source", fromCurrency)
                .build()
            )
            .retrieve()
            .onStatus(status -> (status.value() == 401), response ->
                Mono.error(new ExchangeAPIException("The provided API key was not provided or is invalid."))
            )
            .onStatus(HttpStatusCode::is4xxClientError, response ->
                Mono.error(new ExchangeAPIException("Client error " + response.statusCode()))
            )
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                Mono.error(new ExchangeAPIException("Server error " + response.statusCode()))
            )
            .bodyToMono(ExchangeDetailsDTO.class)
            .doOnSuccess(dto -> LOGGER.info("Received response: {}", dto))
            .block();

        return exchangeDetailsDTO;
    }
}
