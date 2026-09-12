package com.transaction.transactions.Repository.Mongo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.transaction.transactions.Document.FailedTransactionLog;

@Repository
public interface FailedTransactionLogRepository extends MongoRepository<FailedTransactionLog, String> {
    
    List<FailedTransactionLog> findByUserIdOrderByTimestampDesc(Long userId);
    
    List<FailedTransactionLog> findByFailureReasonOrderByTimestampDesc(String failureReason);
    
    List<FailedTransactionLog> findByResolvedFalseOrderByTimestampDesc();
    
    long countByUserIdAndTimestampAfter(Long userId, LocalDateTime since);
}