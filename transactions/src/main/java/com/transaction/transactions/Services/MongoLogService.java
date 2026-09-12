package com.transaction.transactions.Services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.transaction.transactions.Document.FailedTransactionLog;
import com.transaction.transactions.Document.FraudAlert;
import com.transaction.transactions.Document.TransactionLog;
import com.transaction.transactions.Repository.Mongo.FailedTransactionLogRepository;
import com.transaction.transactions.Repository.Mongo.FraudAlertRepository;
import com.transaction.transactions.Repository.Mongo.TransactionLogRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MongoLogService {
    
    @Autowired
    private TransactionLogRepository transactionLogRepository;
    
    @Autowired
    private FailedTransactionLogRepository failedTransactionLogRepository;
    
    @Autowired
    private FraudAlertRepository fraudAlertRepository;
    
    /**
     * Log transaction activity (async)
     */
    @Async
    public void logTransactionActivity(
            String transactionReference,
            Long userId,
            String username,
            String action,
            String transactionType,
            java.math.BigDecimal amount,
            String status,
            String ipAddress,
            String userAgent,
            String deviceFingerprint) {
        
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("action", action);
            metadata.put("status", status);
            
            TransactionLog transactionLog = TransactionLog.builder()
                .transactionReference(transactionReference)
                .userId(userId)
                .username(username)
                .action(action)
                .transactionType(transactionType)
                .amount(amount)
                .status(status)
                .metadata(metadata)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceFingerprint(deviceFingerprint)
                .timestamp(LocalDateTime.now())
                .build();
            
            transactionLogRepository.save(transactionLog);
            log.info("Transaction log saved: {} - {}", transactionReference, action);
            
        } catch (Exception e) {
            log.error("Failed to save transaction log: {}", transactionReference, e);
        }
    }
    
    /**
     * Log failed transaction (async)
     */
    @Async
    public void logFailedTransaction(
            String transactionReference,
            Long userId,
            String username,
            String accountNumber,
            java.math.BigDecimal amount,
            String transactionType,
            String failureReason,
            String errorCode,
            String errorDetails,
            String ipAddress,
            String userAgent) {
        
        try {
            FailedTransactionLog failedLog = FailedTransactionLog.builder()
                .transactionReference(transactionReference)
                .userId(userId)
                .username(username)
                .accountNumber(accountNumber)
                .amount(amount)
                .transactionType(transactionType)
                .failureReason(failureReason)
                .errorCode(errorCode)
                .errorDetails(errorDetails)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .resolved(false)
                .build();
            
            failedTransactionLogRepository.save(failedLog);
            log.warn("Failed transaction logged: {} - {}", transactionReference, failureReason);
            
        } catch (Exception e) {
            log.error("Failed to save failed transaction log: {}", transactionReference, e);
        }
    }
    
    /**
     * Log fraud alert (async)
     */
    @Async
    public void logFraudAlert(
            String transactionReference,
            Long userId,
            String username,
            java.math.BigDecimal amount,
            java.math.BigDecimal fraudScore,
            String riskLevel,
            java.util.List<String> fraudFlags,
            String action,
            String ipAddress,
            String deviceFingerprint) {
        
        try {
            FraudAlert alert = FraudAlert.builder()
                .transactionReference(transactionReference)
                .userId(userId)
                .username(username)
                .amount(amount)
                .fraudScore(fraudScore)
                .riskLevel(riskLevel)
                .fraudFlags(fraudFlags)
                .action(action)
                .ipAddress(ipAddress)
                .deviceFingerprint(deviceFingerprint)
                .detectedAt(LocalDateTime.now())
                .investigated(false)
                .build();
            
            fraudAlertRepository.save(alert);
            log.warn("Fraud alert logged: {} - Risk: {}, Score: {}", 
                transactionReference, riskLevel, fraudScore);
            
        } catch (Exception e) {
            log.error("Failed to save fraud alert: {}", transactionReference, e);
        }
    }
}