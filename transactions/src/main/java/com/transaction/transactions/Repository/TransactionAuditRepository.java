package com.transaction.transactions.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.transaction.transactions.Model.TransactionAuditLog;

@Repository
public interface TransactionAuditRepository extends JpaRepository<TransactionAuditLog, Long> {
    
    // Find all audit logs for a transaction
    List<TransactionAuditLog> findByTransactionIdOrderByCreatedAtDesc(Long transactionId);
    
    // Find by transaction reference
    List<TransactionAuditLog> findByTransactionReferenceOrderByCreatedAtDesc(String transactionReference);
    
    // Find by action type
    List<TransactionAuditLog> findByAction(String action);
    
    // Find audit logs within date range
    @Query("SELECT a FROM TransactionAuditLog a WHERE " +
           "a.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY a.createdAt DESC")
    List<TransactionAuditLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Count failed actions
    @Query("SELECT COUNT(a) FROM TransactionAuditLog a WHERE " +
           "a.status = 'FAILURE' AND " +
           "a.createdAt >= :since")
    long countFailedActions(@Param("since") LocalDateTime since);
}