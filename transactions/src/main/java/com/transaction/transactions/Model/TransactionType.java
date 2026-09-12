package com.transaction.transactions.Model;

public enum TransactionType {
    TRANSFER("Fund transfer between accounts"),
    DEPOSIT("Deposit to account"),
    WITHDRAWAL("Withdrawal from account"),
    PAYMENT("Payment transaction"),
    REFUND("Refund transaction");
    
    private final String description;
    
    TransactionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }

}
