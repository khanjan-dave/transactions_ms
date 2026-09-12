package com.transaction.transactions.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "transaction_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionLog {
    
    @Id
    private String id;
    
    @Indexed
    private String transactionReference;
    
    @Indexed
    private Long userId;
    
    private String username;
    
    @Indexed
    private String action;  // INITIATED, VALIDATED, PROCESSING, COMPLETED, FAILED
    
    private String transactionType;
    private BigDecimal amount;
    private String status;
    
    // Metadata
    private Map<String, Object> metadata;
    
    // Device & Security
    private String ipAddress;
    private String userAgent;
    private String deviceFingerprint;
    
    // Geolocation
    private Map<String, String> location;  // country, city, lat, lng
    
    // Timestamp
    @Indexed(expireAfter = "31536000")
    private LocalDateTime timestamp;
    
    // Error info (if failed)
    private String errorCode;
    private String errorMessage;
}