package com.transaction.transactions.Exception;

public class TransactionLimitExceededException extends RuntimeException {
    public TransactionLimitExceededException(String message) {
        super(message);
    }
}