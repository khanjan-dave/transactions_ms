package com.transaction.transactions.Repository.Mongo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.transaction.transactions.Document.TransactionLog;

@Repository
public interface TransactionLogRepository extends MongoRepository<TransactionLog, String> {
    
    List<TransactionLog> findByTransactionReferenceOrderByTimestampDesc(String transactionReference);
    
    List<TransactionLog> findByUserIdOrderByTimestampDesc(Long userId);
    
    List<TransactionLog> findByActionOrderByTimestampDesc(String action);
    
    List<TransactionLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime start, 
        LocalDateTime end
    );
    
    long countByUserIdAndTimestampAfter(Long userId, LocalDateTime since);
}