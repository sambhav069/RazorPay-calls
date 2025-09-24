package com.example.RazorPay.task.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpiPaymentDto {
    private int amount;
    private String currency;
    private String email;
    private String contact;

    @JsonProperty("order_id")
    private String orderId;
    private String method = "upi";

    private UpiDetails upi;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpiDetails {
    	private String flow;
        private String vpa;  // Virtual Payment Address
    }
}

