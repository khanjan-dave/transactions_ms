package com.transaction.transactions.Dto;

import java.math.BigDecimal;

import com.transaction.transactions.Model.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Sender account number is required")
    @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
    private String senderAccountNumber;
    
    // Receiver info (optional for DEPOSIT/WITHDRAWAL, required for TRANSFER)
    private String receiverAccountNumber;
    
    private String receiverUsername;
    
    // Optional fields
    private String description;
    
    private String notes;
    
    // Device info (for fraud detection)
    private String deviceFingerprint;
    
    // Idempotency key (prevent duplicate transactions)
    private String idempotencyKey;
}
