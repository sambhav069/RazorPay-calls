package com.example.RazorPay.task.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundResponseDto {
    private Map<String, Object> acquirer_data;
    private Integer amount;
    private Integer base_amount;
    private String batch_id;
    private Long created_at;
    private String currency;
    private String entity;
    private String id;
    private List<Object> notes;
    private String payment_id;
    private String receipt;
    private String speed_processed;
    private String speed_requested;
    private String status;
    // getters and setters
}