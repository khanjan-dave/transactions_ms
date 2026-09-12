package com.transaction.transactions.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.transaction.transactions.Model.TransactionStatus;
import com.transaction.transactions.Model.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private String transactionReference;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    
    // Sender info (masked)
    private String senderUsername;
    private String senderAccountNumberMasked;  // ****6789
    private BigDecimal senderBalanceAfter;
    
    // Receiver info (masked)
    private String receiverUsername;
    private String receiverAccountNumberMasked;
    
    private String description;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    
    // Fraud info
    private BigDecimal fraudScore;
    private String riskLevel;  // LOW, MEDIUM, HIGH
    
    private String message;  // Success/error message

}
