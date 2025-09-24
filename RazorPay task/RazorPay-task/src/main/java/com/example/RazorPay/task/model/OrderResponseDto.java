package com.example.RazorPay.task.model;

import java.util.Map;

import lombok.Data;

@Data
public class OrderResponseDto {
    private String id;
    private String entity;
    private Integer amount;
    private Integer amount_paid;
    private Integer amount_due;
    private String currency;
    private String receipt;
    private String status;
    private Integer attempts;
    private Long created_at;
    private Map<String, String> notes; // ✅ handles notes object
    private String offer_id;           // can be null
}
