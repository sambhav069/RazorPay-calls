package com.example.RazorPay.task.schedluer;

import com.example.RazorPay.task.model.paymentTransactionModel;
import com.example.RazorPay.task.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class PaymentReconciliationJob {
    @Autowired
    private PaymentService paymentService;

    @Scheduled(fixedDelay = 50000)
    public void reconcilePendingTransactions() {
        Flux.merge(
                paymentService.reconcileAllActive()
        ).subscribe(
                tx -> System.out.println("Reconciled: " + tx.getPaymentId() +
                        " status=" + tx.getPaymentStatus() +
                        " refundStatus=" + tx.getRefundStatus()),
                err -> System.err.println("Error during reconciliation: " + err.getMessage())
        );
    }

}
