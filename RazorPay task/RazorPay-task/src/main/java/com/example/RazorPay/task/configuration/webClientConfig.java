package com.example.RazorPay.task.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableScheduling
public class webClientConfig {


    private final String keyId = "rzp_test_RIauAdL18LK4NZ";
    private final String keySecret="f8eiyYdiS3tRu1dhN9R4LIMh";

    @Bean
    public WebClient razorPayWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.razorpay.com/v1")
                .defaultHeaders(headers -> headers.setBasicAuth(keyId, keySecret))
                .build();
    }
}