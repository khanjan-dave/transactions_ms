package com.transaction.transactions.Services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.transaction.transactions.Dto.TransactionRequest;
import com.transaction.transactions.Exception.InvalidTransactionException;
import com.transaction.transactions.Exception.TransactionLimitExceededException;
import com.transaction.transactions.Model.TransactionType;
import com.transaction.transactions.Repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionValidationService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Value("${transaction.limit.daily.amount:10000.00}")
    private BigDecimal dailyAmountLimit;
    
    @Value("${transaction.limit.single.amount:5000.00}")
    private BigDecimal singleTransactionLimit;
    
    @Value("${transaction.limit.daily.count:50}")
    private int dailyTransactionCountLimit;
    
    @Value("${transaction.limit.hourly.count:10}")
    private int hourlyTransactionCountLimit;
    
    /**
     * Validate transaction request
     */
    public void validateTransaction(TransactionRequest request, Long userId) {
        
        // Basic validations
        validateBasicFields(request);
        
        // Amount validations
        validateAmount(request.getAmount());
        
        // Single transaction limit
        validateSingleTransactionLimit(request.getAmount());
        
        // Daily amount limit
        validateDailyAmountLimit(userId, request.getAmount());
        
        // Transaction count limits
        validateDailyTransactionCount(userId);
        validateHourlyTransactionCount(userId);
        
        // Type-specific validations
        validateTransactionType(request);
        
        log.info("Transaction validation passed for user {}", userId);
    }
    
    /**
     * Validate basic fields
     */
    private void validateBasicFields(TransactionRequest request) {
        if (request.getAmount() == null) {
            throw new InvalidTransactionException("Transaction amount is required");
        }
        
        if (request.getTransactionType() == null) {
            throw new InvalidTransactionException("Transaction type is required");
        }
        
        if (request.getSenderAccountNumber() == null || request.getSenderAccountNumber().isEmpty()) {
            throw new InvalidTransactionException("Sender account number is required");
        }
    }
    
    /**
     * Validate amount is positive
     */
    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction amount must be greater than zero");
        }
    }
    
    /**
     * Validate single transaction limit
     */
    private void validateSingleTransactionLimit(BigDecimal amount) {
        if (amount.compareTo(singleTransactionLimit) > 0) {
            throw new TransactionLimitExceededException(
                String.format("Single transaction limit exceeded. Maximum allowed: $%.2f", 
                    singleTransactionLimit)
            );
        }
    }
    
    /**
     * Validate daily amount limit
     */
    private void validateDailyAmountLimit(Long userId, BigDecimal newAmount) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        
        BigDecimal todayTotal = transactionRepository.sumDailyAmountByUser(userId, startOfDay);
        BigDecimal newTotal = todayTotal.add(newAmount);
        
        if (newTotal.compareTo(dailyAmountLimit) > 0) {
            throw new TransactionLimitExceededException(
                String.format("Daily transaction limit exceeded. Used: $%.2f, Limit: $%.2f", 
                    todayTotal, dailyAmountLimit)
            );
        }
        
        log.debug("Daily amount check passed for user {}: $%.2f / $%.2f", 
            userId, newTotal, dailyAmountLimit);
    }
    
    /**
     * Validate daily transaction count
     */
    private void validateDailyTransactionCount(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        
        long todayCount = transactionRepository.countDailyTransactionsByUser(userId, startOfDay);
        
        if (todayCount >= dailyTransactionCountLimit) {
            throw new TransactionLimitExceededException(
                String.format("Daily transaction count limit exceeded. Limit: %d transactions per day", 
                    dailyTransactionCountLimit)
            );
        }
        
        log.debug("Daily count check passed for user {}: {} / {}", 
            userId, todayCount, dailyTransactionCountLimit);
    }
    
    /**
     * Validate hourly transaction count
     */
    private void validateHourlyTransactionCount(Long userId) {
        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
        
        long hourlyCount = transactionRepository.countTransactionsInLastHour(userId, lastHour);
        
        if (hourlyCount >= hourlyTransactionCountLimit) {
            throw new TransactionLimitExceededException(
                String.format("Hourly transaction limit exceeded. Limit: %d transactions per hour", 
                    hourlyTransactionCountLimit)
            );
        }
        
        log.debug("Hourly count check passed for user {}: {} / {}", 
            userId, hourlyCount, hourlyTransactionCountLimit);
    }
    
    /**
     * Validate transaction type specific rules
     */
    private void validateTransactionType(TransactionRequest request) {
        TransactionType type = request.getTransactionType();
        
        switch (type) {
            case TRANSFER -> {
                // Transfer requires receiver account
                if (request.getReceiverAccountNumber() == null || 
                    request.getReceiverAccountNumber().isEmpty()) {
                    throw new InvalidTransactionException(
                        "Receiver account number is required for transfer transactions");
                }
                
                // Cannot transfer to same account
                if (request.getSenderAccountNumber().equals(request.getReceiverAccountNumber())) {
                    throw new InvalidTransactionException(
                        "Cannot transfer to the same account");
                }
            }
                
            case DEPOSIT -> {
                // Deposit doesn't need receiver
            }
                
            case WITHDRAWAL -> {
                // Withdrawal doesn't need receiver
            }
                
            case PAYMENT -> {
                // Payment requires receiver
                if (request.getReceiverAccountNumber() == null || 
                    request.getReceiverAccountNumber().isEmpty()) {
                    throw new InvalidTransactionException(
                        "Receiver account number is required for payment transactions");
                }
            }
                
            default -> throw new InvalidTransactionException("Invalid transaction type");
        }
    }
    
    /**
     * Validate account number format
     */
    public void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new InvalidTransactionException("Account number cannot be empty");
        }
        
        if (accountNumber.length() < 10 || accountNumber.length() > 20) {
            throw new InvalidTransactionException(
                "Account number must be between 10 and 20 characters");
        }
        
        if (!accountNumber.matches("^[0-9]+$")) {
            throw new InvalidTransactionException("Account number must contain only digits");
        }
    }
}