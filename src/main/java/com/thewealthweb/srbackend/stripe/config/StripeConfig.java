package com.thewealthweb.srbackend.stripe.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class StripeConfig {

    @Value("${stripe.connect.client-secret}")
    private String stripeClientSecret;

    // Optional: Configure a WebClient for external HTTP calls (like OAuth token exchange)
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // This bean initializes Stripe's API key for direct API calls using stripe-java library
    @Bean
    public String setupStripeApiKey() {
        Stripe.apiKey = stripeClientSecret;
        return stripeClientSecret;
    }
}
