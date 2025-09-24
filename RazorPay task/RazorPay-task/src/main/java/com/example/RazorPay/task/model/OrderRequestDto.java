package com.example.RazorPay.task.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {
	private int amount;
	private String currency;
	private String receipt;
	private Map<String, String> notes;
}
