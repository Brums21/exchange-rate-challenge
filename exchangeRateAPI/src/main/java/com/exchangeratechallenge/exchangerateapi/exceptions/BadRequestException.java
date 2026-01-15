package com.exchangeratechallenge.exchangerateapi.exceptions;

/**
 * Custom exception class to handle bad request scenarios.
 */
public class BadRequestException extends RuntimeException {
    
    /**
     * Constructs a new BadRequestException with the specified detail message.
     *
     * @param message The detail message.
     */
    public BadRequestException(String message) {
        super(message);
    }
    
}
