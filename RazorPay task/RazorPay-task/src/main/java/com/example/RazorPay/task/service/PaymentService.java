package com.example.RazorPay.task.service;

import com.example.RazorPay.task.model.*;
import com.example.RazorPay.task.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaymentService {

    private final WebClient razorPay;
    private final orderIdService orderIdService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public PaymentService(WebClient razorPay,
                          orderIdService orderIdService,
                          PaymentTransactionRepository paymentTransactionRepository) {
        this.razorPay = razorPay;
        this.orderIdService = orderIdService;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }


    private String extractValue(String source, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(source);
        return matcher.find() ? matcher.group(1) : null;
    }

    private paymentTransactionModel buildTransaction(String orderId, String paymentId, int amount) {
        paymentTransactionModel tx = new paymentTransactionModel();
        tx.setOrderId(orderId);
        tx.setPaymentId(paymentId);
        tx.setPaymentStatus("PENDING");
        tx.setAmount(amount);
        tx.setRefundId(null);
        tx.setRefundStatus(null);
        return tx;
    }

    private Integer safeParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private OrderRequestDto buildOrderRequest(int amount, String currency) {
        OrderRequestDto orderRequest = new OrderRequestDto();
        orderRequest.setAmount(amount);
        orderRequest.setCurrency(currency);
        orderRequest.setReceipt("Receipt no. 1");

        Map<String, String> notes = new HashMap<>();
        notes.put("notes_key_1", "Tea, Earl Grey, Hot");
        notes.put("notes_key_2", "Tea, Earl Grey… decaf.");
        orderRequest.setNotes(notes);

        return orderRequest;
    }

    public Mono<OrderResponseDto> createOrder(OrderRequestDto orders) {
        return razorPay.post()
                .uri("/orders")
                .bodyValue(orders)
                .retrieve()
                .bodyToMono(OrderResponseDto.class)
                .flatMap(order -> {
                    orderIdService.setOrder_id(Mono.just(order.getId()));

                    paymentTransactionModel tx = new paymentTransactionModel();
                    tx.setOrderId(order.getId());
                    tx.setAmount(orders.getAmount());
                    tx.setPaymentId(null);
                    tx.setPaymentStatus("PENDING");
                    tx.setRefundId(null);
                    tx.setRefundStatus(null);

                    return paymentTransactionRepository.save(tx)
                            .thenReturn(order);
                });
    }

    public Mono<String> processCardPayment(CardPaymentDto dto) {
        OrderRequestDto orderRequest = buildOrderRequest(dto.getAmount(), dto.getCurrency());

        return razorPay.post()
                .uri("/orders")
                .bodyValue(orderRequest)
                .retrieve()
                .bodyToMono(OrderResponseDto.class)
                .flatMap(order -> {
                    dto.setOrderId(order.getId());
                    orderIdService.setOrder_id(Mono.just(order.getId()));

                    return razorPay.post()
                            .uri("/payments/create")
                            .bodyValue(dto)
                            .retrieve()
                            .bodyToMono(String.class)
                            .flatMap(html -> razorPay.get()
                                    .uri("/orders/" + order.getId() + "/payments")
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .map(json -> extractValue(json, "id"))
                                    .flatMap(paymentId -> {
                                        if (paymentId == null) {
                                            return Mono.error(new IllegalStateException("Payment ID not found for card order " + order.getId()));
                                        }
                                        paymentTransactionModel tx = buildTransaction(order.getId(), paymentId, dto.getAmount());
                                        return paymentTransactionRepository.save(tx)
                                                .thenReturn(html);
                                    }));
                });
    }

    public Mono<String> processUpiPayment(UpiPaymentDto dto) {
        OrderRequestDto orderRequest = buildOrderRequest(dto.getAmount(), dto.getCurrency());

        return razorPay.post()
                .uri("/orders")
                .bodyValue(orderRequest)
                .retrieve()
                .bodyToMono(OrderResponseDto.class)
                .flatMap(order -> {
                    dto.setOrderId(order.getId());
                    orderIdService.setOrder_id(Mono.just(order.getId()));

                    return razorPay.post()
                            .uri("/payments/create")
                            .bodyValue(dto)
                            .retrieve()
                            .bodyToMono(String.class)
                            .flatMap(html -> {
                                String paymentId = extractValue(html, "payment_id");
                                if (paymentId == null) {
                                    return Mono.error(new IllegalStateException("Payment ID not found for UPI order " + order.getId()));
                                }
                                paymentTransactionModel tx = buildTransaction(order.getId(), paymentId, dto.getAmount());
                                return paymentTransactionRepository.save(tx)
                                        .thenReturn(html);
                            });
                });
    }

    public Mono<RefundResponseDto> issueRefund(String paymentId, int amount) {
        return razorPay.post()
                .uri("/payments/" + paymentId + "/refund")
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .bodyToMono(RefundResponseDto.class)
                .flatMap(dto -> paymentTransactionRepository.findByPaymentId(paymentId)
                        .flatMap(tx -> {
                            tx.setRefundId(dto.getId());
                            tx.setRefundStatus(dto.getStatus()); // Razorpay’s raw status
                            return paymentTransactionRepository.save(tx)
                                    .thenReturn(dto);
                        }));
    }

    private Mono<paymentTransactionModel> updatePaymentFromApi(paymentTransactionModel tx) {
        return razorPay.get()
                .uri("/payments/" + tx.getPaymentId())
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(json -> {
                    String status       = extractValue(json, "status");        // "captured", "failed", "refunded"
                    String refundId     = extractValue(json, "refund_id");
                    String refundStatus = extractValue(json, "refund_status"); // "full", "partial"
                    String amountStr    = extractValue(json, "amount");
                    Integer amount      = (amountStr != null) ? safeParseInt(amountStr) : null;

                    // Persist Razorpay’s original values
                    tx.setPaymentStatus(status);       // <-- will now become "refunded"
                    tx.setRefundId(refundId);
                    tx.setRefundStatus(refundStatus);  // <-- will now become "full" or "partial"
                    if (amount != null) tx.setAmount(amount);

                    return paymentTransactionRepository.save(tx);
                });
    }

    private Mono<paymentTransactionModel> updateRefundFromApi(paymentTransactionModel tx) {
        return razorPay.get()
                .uri("/refunds/" + tx.getRefundId())
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(json -> {
                    String refundStatus = extractValue(json, "status");
                    tx.setRefundStatus(refundStatus);
                    return paymentTransactionRepository.save(tx);
                });
    }
    public Flux<paymentTransactionModel> reconcileActiveRefunds() {

            return paymentTransactionRepository.findAll()
                    .filter(tx -> {
                        // Only reconcile if not in terminal state
                        boolean paymentActive = tx.getPaymentStatus() != null &&
                                !tx.getPaymentStatus().equalsIgnoreCase("failed") &&
                                !tx.getPaymentStatus().equalsIgnoreCase("refunded");
                        boolean refundActive = tx.getRefundId() != null &&
                                (tx.getRefundStatus() == null ||
                                        (!tx.getRefundStatus().equalsIgnoreCase("failed")
                                                && !tx.getRefundStatus().equalsIgnoreCase("full")));
                        return paymentActive || refundActive;
                    })
                    .flatMap(this::updatePaymentFromApi); // <-- always hit /payments/{id}
        }


    public Flux<paymentTransactionModel> reconcileActivePayments(){
        return paymentTransactionRepository.findAll()
                .filter(tx ->tx.getPaymentId() != null && !tx.getPaymentId().isBlank())
                .filter(tx -> tx.getPaymentStatus() != null
                        &&  !tx.getPaymentStatus().equalsIgnoreCase("captured")
                        &&  !tx.getPaymentStatus().equalsIgnoreCase("failed"))
                .flatMap(this::updatePaymentFromApi);
    }


    public Flux<paymentTransactionModel> reconcileAllActive() {
        return Flux.merge(
                reconcileActivePayments(),
                reconcileActiveRefunds()
        );
    }
}