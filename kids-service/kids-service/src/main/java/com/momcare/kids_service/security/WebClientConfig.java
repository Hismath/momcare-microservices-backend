package com.momcare.kids_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081")  // Set a default base URL (optional)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)  // Default headers
                .filter(loggingFilter())  // Applying custom filter
                .filter(errorHandlingFilter()); // Optional: Add a filter for error handling
    }

    /**
     * Custom filter for logging request details.
     */
    private ExchangeFilterFunction loggingFilter() {
        return (request, next) -> {
            // Log request URL
            System.out.println("Request: " + request.url());
            return next.exchange(request)
                    .doOnTerminate(() -> System.out.println("Request completed for: " + request.url()));
        };
    }

    /**
     * Optional filter for handling errors.
     */
    private ExchangeFilterFunction errorHandlingFilter() {
        return (request, next) -> next.exchange(request)
                .doOnError(error -> {
                    System.out.println("Error occurred during the request: " + error.getMessage());
                });
    }
}
