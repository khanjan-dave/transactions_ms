package com.transaction.transactions.Repository.Mongo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.transaction.transactions.Document.FraudAlert;

@Repository
public interface FraudAlertRepository extends MongoRepository<FraudAlert, String> {
    
    List<FraudAlert> findByUserIdOrderByDetectedAtDesc(Long userId);
    
    List<FraudAlert> findByRiskLevelOrderByDetectedAtDesc(String riskLevel);
    
    List<FraudAlert> findByInvestigatedFalseOrderByDetectedAtDesc();
    
    List<FraudAlert> findByDetectedAtBetweenOrderByDetectedAtDesc(
        LocalDateTime start,
        LocalDateTime end
    );
    
    long countByUserIdAndDetectedAtAfter(Long userId, LocalDateTime since);
}