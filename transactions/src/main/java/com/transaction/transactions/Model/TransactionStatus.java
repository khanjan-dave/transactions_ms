package com.transaction.transactions.Model;

public enum TransactionStatus {
    PENDING("Transaction initiated, awaiting processing"),
    VALIDATING("Validating transaction details"),
    PROCESSING("Processing transaction"),
    COMPLETED("Transaction completed successfully"),
    FAILED("Transaction failed"),
    REVERSED("Transaction reversed/refunded"),
    CANCELLED("Transaction cancelled by user");
    
    private final String description;
    
    TransactionStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }

}
