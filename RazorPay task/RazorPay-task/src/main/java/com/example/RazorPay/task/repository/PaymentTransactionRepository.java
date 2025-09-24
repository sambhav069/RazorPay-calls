package com.example.RazorPay.task.repository;

import com.example.RazorPay.task.model.paymentTransactionModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentTransactionRepository extends ReactiveCrudRepository<paymentTransactionModel, Long> {

    Flux<paymentTransactionModel> findByPaymentStatus(String paymentStatus);


    @NonNull
    Mono<paymentTransactionModel> findByPaymentId(@NonNull String paymentId);


    Mono<paymentTransactionModel> findByRefundStatus(@NonNull String pending);
}