package com.transaction.transactions.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "fraud_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlert {
    
    @Id
    private String id;
    
    @Indexed
    private String transactionReference;
    
    @Indexed
    private Long userId;
    
    private String username;
    private BigDecimal amount;
    
    // Fraud details
    private BigDecimal fraudScore;
    private String riskLevel;  // LOW, MEDIUM, HIGH, CRITICAL
    private List<String> fraudFlags;
    
    // Action taken
    @Indexed
    private String action;  // BLOCKED, FLAGGED, REVIEWED, CLEARED
    
    private String ipAddress;
    private String deviceFingerprint;
    
    @Indexed
    private LocalDateTime detectedAt;
    
    // Investigation
    private boolean investigated;
    private String investigationNotes;
    private LocalDateTime investigatedAt;
}
