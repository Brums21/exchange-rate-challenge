package com.exchangeratechallenge.exchangeRateAPI.utils;

import com.exchangeratechallenge.exchangeRateAPI.models.DTOs.ExchangeAPISymbolsDTO;

import com.exchangeratechallenge.exchangeRateAPI.exceptions.BadRequestException;

public class ParameterCleaner {

    private ParameterCleaner() {}

    /**
     * Cleans and validates a currency string against accepted symbols.
     *
     * @param currency   The currency string to clean and validate.
     * @param symbolsDTO The DTO containing accepted currency symbols.
     * @return The cleaned currency string.
     * @throws BadRequestException if the currency is not accepted, meaning it's not present in the external API's list of accepted symbols.
     */
    public static String cleanAndValidateCurrency(String currency, ExchangeAPISymbolsDTO symbolsDTO) {
        currency = cleanString(currency);
        if (!symbolsDTO.hasSymbol(currency)) {
            throw new BadRequestException("Currency " + currency + " is not accepted");
        }

        return currency;
    }

    /**
    * Auxiliary method to clean and validate currency strings.
    * @param currency The currency string to clean and validate.
    * @return The cleaned currency string, with no quotes or apostrophes, converted to upper case.
    */
    private static String cleanString(String currency) {
        return currency.trim()
            .replace("\"", "")
            .replace("'","")
            .toUpperCase();
    }
}