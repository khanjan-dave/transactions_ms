package com.transaction.transactions.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "transaction_audit_logs", indexes = {
    @Index(name = "idx_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;
    
    @Column(name = "transaction_reference", nullable = false, length = 50)
    private String transactionReference;
    
    @Column(name = "action", nullable = false, length = 50)
    private String action;  // INITIATED, VALIDATED, COMPLETED, FAILED, REVERSED
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
