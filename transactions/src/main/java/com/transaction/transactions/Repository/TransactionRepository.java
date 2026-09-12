package com.transaction.transactions.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.transaction.transactions.Model.TransactionModel;
import com.transaction.transactions.Model.TransactionStatus;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionModel, Long> {
    
    // Find by transaction reference
    Optional<TransactionModel> findByTransactionReference(String transactionReference);
    
    // Check if transaction reference exists (for idempotency)
    boolean existsByTransactionReference(String transactionReference);
    
    // Find by sender
    List<TransactionModel> findBySenderUserIdOrderByInitiatedAtDesc(Long senderUserId);
    
    List<TransactionModel> findBySenderAccountNumberOrderByInitiatedAtDesc(String senderAccountNumber);
    
    // Find by receiver
    List<TransactionModel> findByReceiverUserIdOrderByInitiatedAtDesc(Long receiverUserId);
    
    // Find by status
    List<TransactionModel> findByStatus(TransactionStatus status);
    
    // Find by user (sender or receiver)
    @Query("SELECT t FROM TransactionModel t WHERE " +
           "t.senderUserId = :userId OR t.receiverUserId = :userId " +
           "ORDER BY t.initiatedAt DESC")
    List<TransactionModel> findByUserId(@Param("userId") Long userId);
    
    // Find transactions within date range
    @Query("SELECT t FROM TransactionModel t WHERE " +
           "t.initiatedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.initiatedAt DESC")
    List<TransactionModel> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Count transactions by user today
    @Query("SELECT COUNT(t) FROM TransactionModel t WHERE " +
           "t.senderUserId = :userId AND " +
           "t.initiatedAt >= :startOfDay AND " +
           "t.status = 'COMPLETED'")
    long countDailyTransactionsByUser(
        @Param("userId") Long userId,
        @Param("startOfDay") LocalDateTime startOfDay
    );
    
    // Sum of transaction amounts by user today
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionModel t WHERE " +
           "t.senderUserId = :userId AND " +
           "t.initiatedAt >= :startOfDay AND " +
           "t.status = 'COMPLETED'")
    java.math.BigDecimal sumDailyAmountByUser(
        @Param("userId") Long userId,
        @Param("startOfDay") LocalDateTime startOfDay
    );
    
    // Count transactions in last hour (velocity check)
    @Query("SELECT COUNT(t) FROM TransactionModel t WHERE " +
           "t.senderUserId = :userId AND " +
           "t.initiatedAt >= :lastHour")
    long countTransactionsInLastHour(
        @Param("userId") Long userId,
        @Param("lastHour") LocalDateTime lastHour
    );
    
    // Find pending transactions older than X minutes
    @Query("SELECT t FROM TransactionModel t WHERE " +
           "t.status = 'PENDING' AND " +
           "t.initiatedAt < :cutoffTime")
    List<TransactionModel> findStalePendingTransactions(
        @Param("cutoffTime") LocalDateTime cutoffTime
    );
}