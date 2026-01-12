# Exchange Rate Challenge

This project consists of the development of a RESTful API built with Spring Boot that retrieves currency exchange rates from the publicly available [exchangerate.host](https://exchangerate.host) API.

The API allows users to: 

- Retrieve all exchange rates for a given base currency;
- Retrieve the exchange rate between two specific currencies.

## Project overview

The application was developed using the **Spring Boot framework**, following REST best practices and a layered architecture. Special emphasis was placed on software quality, test coverage, and continuous integration.

The project includes:
- **Unit tests** to validate service-level behaviour;
- **Integration tests** to verify the full application context;
- **Boundary (controller) tests** to validate API endpoints and request handling.

A CI/CD pipeline was implemented to automatically analyse the codebase on every push or pull request to the main branch.

### Code quality and CI/CD

The CI/CD pipeline integrates SonarQube to ensure code quality and maintainability throughout the development process.

The default quality gate enforces the following conditions:

- No new bugs;
- No security vulnerabilities;
- Limited technical debt;
- All security hotspots reviewed;
- Code coverage greater than 80%;
- Limited code duplication.

This ensures that all committed code adheres to clean code and software engineering best practices.

### Available Endpoints

#### `GET /api/v1/exchange/rate`

Returns the exchange rate between two different currencies, provided by two required parameters, `fromCurrency` and `toCurrency`.
First, the currencies are validated and verified against the set of available currencies, returning an error message if not present. Then, all available exchange rates are extracted from the external API. Finally, `toCurrency` value is searched and extracted upon the available exchange rates. If no `toCurrency` value is found from the list, then returns a message indicating that this currency was not found.

#### `GET /api/v1/exchange/rates`

Returns the exchange rate for one currency, across different available currencies, provided by the required parameter `fromCurrency`.
Similarly to the previous endpoint, the currency is validated and verified against the set of available currencies, and returning an error message if not present. Then, all available exchage rates are extracted from the external API and displayed through this endpoint.

## Requirements

This project requires the following programs to be installed in your current machine:

- Java (JDK 21)
- Apache Maven (Apache Maven 3.9.11) - you can install it [here](https://maven.apache.org/install.html).
