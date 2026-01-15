package com.exchangeratechallenge.exchangerateapi.exceptions;

/**
 * Custom exception class to handle external exchange API errors.
 */
public class ExchangeAPIException extends RuntimeException {
    
    /**
     * Constructs a new ExchangeAPIException with the specified detail message.
     *
     * @param message The detail message.
     */
    public ExchangeAPIException(String message) {
        super(message);
    }
}   