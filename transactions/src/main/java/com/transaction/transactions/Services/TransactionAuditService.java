package com.transaction.transactions.Services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.transaction.transactions.Model.TransactionAuditLog;
import com.transaction.transactions.Repository.TransactionAuditRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionAuditService {
    
    @Autowired
    private TransactionAuditRepository auditRepository;
    
    /**
     * Log transaction audit event (async)
     */
    @Async
    public void logAudit(
            Long transactionId,
            String transactionReference,
            String action,
            String status,
            String details,
            String ipAddress,
            String userAgent) {
        
        try {
            TransactionAuditLog auditLog = TransactionAuditLog.builder()
                .transactionId(transactionId)
                .transactionReference(transactionReference)
                .action(action)
                .status(status)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
            
            auditRepository.save(auditLog);
            
            log.info("Audit log saved: {} - {} - {}", transactionReference, action, status);
            
        } catch (Exception e) {
            log.error("Failed to save audit log for transaction {}: {}", 
                transactionReference, e.getMessage());
        }
    }
    
    /**
     * Log audit with error
     */
    @Async
    public void logAuditWithError(
            Long transactionId,
            String transactionReference,
            String action,
            String errorMessage,
            String ipAddress,
            String userAgent) {
        
        try {
            TransactionAuditLog auditLog = TransactionAuditLog.builder()
                .transactionId(transactionId)
                .transactionReference(transactionReference)
                .action(action)
                .status("FAILURE")
                .details("Transaction failed")
                .errorMessage(errorMessage)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
            
            auditRepository.save(auditLog);
            
            log.warn("Audit log saved with error: {} - {} - {}", 
                transactionReference, action, errorMessage);
            
        } catch (Exception e) {
            log.error("Failed to save error audit log for transaction {}: {}", 
                transactionReference, e.getMessage());
        }
    }
}