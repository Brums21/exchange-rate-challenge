package com.exchangeratechallenge.exchangeRateAPI.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.exchangeratechallenge.exchangeRateAPI.exceptions.ExchangeAPIException;
import com.exchangeratechallenge.exchangeRateAPI.models.DTOs.ExchangeAPIResponseDTO;
import com.exchangeratechallenge.exchangeRateAPI.models.DTOs.ExchangeAPISymbolsDTO;

import reactor.core.publisher.Mono;

/**
 * Service class to interact with the external exchange rate API.
 */
@Service
public class ExchangeExternalAPIService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeExternalAPIService.class);

    private final WebClient webClient;
    private final String apiKey;

    public ExchangeExternalAPIService(WebClient webClient, @Value("${exchangeAPI}") String apiKey) {
        this.webClient = webClient;
        this.apiKey = apiKey;

        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalStateException("Missing configuration: exchangeAPI (API key) is not set");
        }
    }

    /**
     * Fetches all available exchange rates from the external API for a given source currency.
     *
     * @param fromCurrency The source currency code.
     * @return An ExchangeAPIResponseDTO containing the exchange rates.
     */
    public ExchangeAPIResponseDTO getExchangeRate(String fromCurrency) {

        LOGGER.info("Fetching exchange rate from currency");

        return webClient.get()
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
            .bodyToMono(ExchangeAPIResponseDTO.class)
            .doOnSuccess(dto -> LOGGER.info("Received successful response from external API /live endpoint"))
            .block();

    }

    /**
     * Fetches the list of accepted currency symbols from the external API.
     *
     * @return An ExchangeAPISymbolsDTO containing the accepted symbols.
     */
    public ExchangeAPISymbolsDTO getAcceptedSymbols() {
        LOGGER.info("Fetching accepted symbols");

        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/list")
                .queryParam("access_key", apiKey.trim())
                .build()
            )
            .retrieve()
            .onStatus(status -> (status.value() == 401), response ->
                Mono.error(new ExchangeAPIException("The provided API key was not provided or is invalid."))
            )
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                Mono.error(new ExchangeAPIException("Server error " + response.statusCode()))
            )
            .bodyToMono(ExchangeAPISymbolsDTO.class)
            .doOnSuccess(dto -> LOGGER.info("Received successful response from /symbols endpoint", dto))
            .block();

    }
    
}
