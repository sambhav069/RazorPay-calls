package com.example.RazorPay.task.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("payment_transactions")

public class paymentTransactionModel {
    @Id
    @Column("id")
    private Long id;

    @Column("payment_id")
    private String paymentId;

    @Column("order_id")
    private String orderId;

    @Column("payment_status")
    private String paymentStatus;

    @Column("amount")
    private Integer amount;

    @Column("refund_id")
    private String refundId;

    @Column("refund_status")
    private String refundStatus;
}




