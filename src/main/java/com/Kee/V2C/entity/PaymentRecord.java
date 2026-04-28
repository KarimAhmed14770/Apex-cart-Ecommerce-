package com.Kee.V2C.entity;


import com.Kee.V2C.enums.PaymentStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_records")
@EntityListeners(AuditingEntityListener.class)
public class PaymentRecord {

    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private String idempotencyKey; // The unique shield

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private PaymentStatus status; // SUCCESS, FAILED, PENDING



    @Column(name = "amount")
    private BigDecimal amount;


    @Column(name = "transaction_reference")
    private String transactionReference;


    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order; // Link it to the specific order

    public PaymentRecord(){}

    public PaymentRecord(String idempotencyKey,BigDecimal amount, PaymentStatus status){
        this.idempotencyKey=idempotencyKey;
        this.amount=amount;
        this.status=status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
