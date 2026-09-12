package com.transaction.transactions.Exception;

public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String message) {
        super(message);
    }
}