package com.exchangeRateChallenge.exchangeRateAPI.exceptions;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ExchangeAPIException.class)
    public ResponseEntity<ErrorResponse> handleExchangeAPIException(ExchangeAPIException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(new ErrorResponse(
                "Error communicating with Exchange Rate API: " + ex.getMessage(),
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage(), Instant.now().toString()));
    }

}
