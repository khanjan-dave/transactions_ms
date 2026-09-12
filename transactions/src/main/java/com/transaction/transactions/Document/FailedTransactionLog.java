package com.transaction.transactions.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "failed_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedTransactionLog {
    
    @Id
    private String id;
    
    @Indexed
    private String transactionReference;
    
    @Indexed
    private Long userId;
    
    private String username;
    private String accountNumber;
    private BigDecimal amount;
    private String transactionType;
    
    // Failure details
    @Indexed
    private String failureReason;
    private String errorCode;
    private String errorDetails;
    
    // Context
    private String ipAddress;
    private String userAgent;
    
    @Indexed
    private LocalDateTime timestamp;
    
    // Investigation
    private boolean resolved;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;
}