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

@ConditionalOnProperty(
    name = "rate-limiter.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@Component
public class ThrottlingFilter implements Filter {

    private ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        return Bucket.builder()
            .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(1)))
            .build();
    }

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