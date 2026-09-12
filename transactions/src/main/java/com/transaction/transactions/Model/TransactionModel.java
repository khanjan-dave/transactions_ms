package com.transaction.transactions.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_reference", columnList = "transaction_reference"),
    @Index(name = "idx_sender_user_id", columnList = "sender_user_id"),
    @Index(name = "idx_receiver_user_id", columnList = "receiver_user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_initiated_at", columnList = "initiated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    // Transaction Identifiers
    @Column(name = "transaction_reference", unique = true, nullable = false, length = 50)
    private String transactionReference;  // TXN20260309123456
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "USD";
    
    // Sender Information
    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;
    
    @Column(name = "sender_username", nullable = false, length = 50)
    private String senderUsername;
    
    @Column(name = "sender_account_number", nullable = false, length = 20)
    private String senderAccountNumber;
    
    @Column(name = "sender_balance_before", precision = 15, scale = 2)
    private BigDecimal senderBalanceBefore;

    @Column(name = "sender_balance_after", precision = 15, scale = 2)
    private BigDecimal senderBalanceAfter;
    
    // Receiver Information
    @Column(name = "receiver_user_id")
    private Long receiverUserId;
    
    @Column(name = "receiver_username", length = 50)
    private String receiverUsername;
    
    @Column(name = "receiver_account_number", length = 20)
    private String receiverAccountNumber;
    
    @Column(name = "receiver_balance_before", precision = 15, scale = 2)
    private BigDecimal receiverBalanceBefore;
    
    @Column(name = "receiver_balance_after", precision = 15, scale = 2)
    private BigDecimal receiverBalanceAfter;
    
    // Transaction Status & Metadata
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Security & Audit
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;
    
    // Fraud Detection
    @Column(name = "fraud_score", precision = 3, scale = 2)
    private BigDecimal fraudScore;
    
    @Column(name = "fraud_flags", columnDefinition = "TEXT")
    private String fraudFlags;
    
    // Timestamps
    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        initiatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}