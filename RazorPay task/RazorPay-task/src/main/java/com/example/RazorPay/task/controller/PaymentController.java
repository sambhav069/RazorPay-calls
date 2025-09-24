package com.example.RazorPay.task.controller;

import com.example.RazorPay.task.model.*;
import com.example.RazorPay.task.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("orderDetails")
public class PaymentController {

    @Autowired
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public Mono<OrderResponseDto> getOrders(@RequestBody OrderRequestDto orders) {
        return paymentService.createOrder(orders);
    }

    @PostMapping("/payment/card")
    public Mono<String> processCard(@RequestBody CardPaymentDto dto) {
        return paymentService.processCardPayment(dto);
    }

    @PostMapping("/payment/upi")
    public Mono<String> processUpi(@RequestBody UpiPaymentDto dto) {
        return paymentService.processUpiPayment(dto);
    }

    @PostMapping("/refund/{payment_id}")
    public Mono<RefundResponseDto> requestRefund(@PathVariable String payment_id,
                                                 @RequestBody Map<String, Object> body) {
        Integer amount = (Integer) body.get("amount");
        if (amount == null) {
            return Mono.error(new IllegalArgumentException("amount is required"));
        }
        return paymentService.issueRefund(payment_id, amount);
    }
}