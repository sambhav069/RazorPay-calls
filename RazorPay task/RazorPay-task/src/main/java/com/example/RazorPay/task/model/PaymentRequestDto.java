package com.example.RazorPay.task.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDto {
	private String callbackUrl;
    private int amount;
    private String method; // e.g., "card", "upi"
    private String contact;
    private String currency;
    private String orderId;
    private String email;

    // Holds raw JSON payload for method-specific data
    private Map<String, Object> paymentDetails;

}
