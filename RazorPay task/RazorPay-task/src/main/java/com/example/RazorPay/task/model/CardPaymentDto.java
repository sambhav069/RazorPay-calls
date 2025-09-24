package com.example.RazorPay.task.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardPaymentDto {
    private int amount;
    private String currency;
    private String email;
    private String contact;
    
    @JsonProperty("order_id")
    private String orderId;
    
    private String method = "card"; // default to "card"

    private CardDetails card;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CardDetails {
        private String number;
        
        @JsonProperty("expiry_month")
        private String expiryMonth;
        
        @JsonProperty("expiry_year")
        private String expiryYear;
        private String cvv;
        private String name;
    }
}

