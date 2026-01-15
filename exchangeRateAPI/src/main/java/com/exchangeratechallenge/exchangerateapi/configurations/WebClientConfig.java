package com.exchangeratechallenge.exchangerateapi.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Configuration class to set up WebClient for external API communication */
@Configuration
public class WebClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientConfig.class);

    /* Configures and provides a WebClient bean with the specified base URL */
    @Bean
    public WebClient exchangeRateWebClient( @Value("${exchange-url:https://api.exchangerate.host}") String baseUrl) {
        LOGGER.info("Configuring WebClient with base URL: {}", baseUrl);
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
