package com.exchangeratechallenge.exchangerateapi.configurations;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A filter that implements rate limiting using Bucket4j library.
 * Limits the number of requests from a single IP address to 5 requests per minute.
 */
@ConditionalOnProperty(
    name = "rate-limiter.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@Component
public class ThrottlingFilter implements Filter {

    private ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Creates a new Bucket with a limit of 5 requests per minute.
     * @return a new Bucket instance
     */
    private Bucket createNewBucket() {
        return Bucket.builder()
            .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(1)))
            .build();
    }

    /**
     * Filters incoming requests and applies rate limiting based on the client's IP address.
     * If the request exceeds the limit, responds with HTTP 429 Too Many Requests.
     * If a client isn't present in the bucket, initializes a new bucket for them, which is stored in the buckets map.
     * @param servletRequest  the incoming servlet request
     * @param servletResponse the outgoing servlet response
     * @param filterChain     the filter chain to pass the request/response to the next filter
     * @throws IOException      if an I/O error occurs during filtering
     * @throws ServletException if a servlet error occurs during filtering
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        String appKey = httpRequest.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(appKey, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            httpResponse.setContentType("application/json");
            httpResponse.setStatus(429);
            httpResponse.getWriter().append("{\"message\":\"Too many requests\"}");
        }
    }

}