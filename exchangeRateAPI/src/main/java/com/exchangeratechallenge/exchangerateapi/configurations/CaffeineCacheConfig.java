package com.exchangeratechallenge.exchangerateapi.configurations;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/* Configuration class to set up Caffeine caching */
@Configuration
@EnableCaching
public class CaffeineCacheConfig {
    
    /**
     * Configures Caffeine cache with an expiration time of 1 minute.
     * @return a Caffeine configuration instance
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES);
    }

    /**
     * Creates a CacheManager bean using the Caffeine configuration.
     * @param caffeine the Caffeine configuration
     * @return a CacheManager instance
     */
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}
