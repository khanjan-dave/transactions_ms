package com.transaction.transactions.Services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.transaction.transactions.Dto.FraudCheckRequest;
import com.transaction.transactions.Dto.FraudCheckResponse;
import com.transaction.transactions.Exception.FraudDetectedException;
import com.transaction.transactions.Repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FraudDetectionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private MongoLogService mongoLogService;
    
    @Value("${fraud.detection.enabled:true}")
    private boolean fraudDetectionEnabled;
    
    @Value("${fraud.detection.threshold.amount:2000.00}")
    private BigDecimal highAmountThreshold;
    
    @Value("${fraud.detection.velocity.count:5}")
    private int velocityThresholdCount;
    
    @Value("${fraud.detection.velocity.window.minutes:15}")
    private int velocityWindowMinutes;
    
    /**
     * Perform comprehensive fraud detection
     */
    public FraudCheckResponse checkFraud(FraudCheckRequest request) {
        
        if (!fraudDetectionEnabled) {
            return FraudCheckResponse.builder()
                .transactionReference(request.getTransactionReference())
                .fraudScore(BigDecimal.ZERO)
                .riskLevel("LOW")
                .flags(new ArrayList<>())
                .recommendation("APPROVE")
                .reason("Fraud detection disabled")
                .build();
        }
        
        List<String> fraudFlags = new ArrayList<>();
        BigDecimal fraudScore = BigDecimal.ZERO;
        
        // Check 1: High amount transaction
        fraudScore = fraudScore.add(checkHighAmountTransaction(request, fraudFlags));
        
        // Check 2: Velocity check (too many transactions in short time)
        fraudScore = fraudScore.add(checkVelocity(request, fraudFlags));
        
        // Check 3: Unusual time (transactions at odd hours)
        fraudScore = fraudScore.add(checkUnusualTime(fraudFlags));
        
        // Check 4: Round amount (exactly $1000, $5000, etc.)
        fraudScore = fraudScore.add(checkRoundAmount(request, fraudFlags));
        
        // Normalize fraud score to 0-1 range
        fraudScore = fraudScore.min(BigDecimal.ONE).setScale(2, RoundingMode.HALF_UP);
        
        // Determine risk level
        String riskLevel = calculateRiskLevel(fraudScore);
        
        // Determine recommendation
        String recommendation = determineRecommendation(fraudScore, riskLevel);
        
        FraudCheckResponse response = FraudCheckResponse.builder()
            .transactionReference(request.getTransactionReference())
            .fraudScore(fraudScore)
            .riskLevel(riskLevel)
            .flags(fraudFlags)
            .recommendation(recommendation)
            .reason(buildReasonMessage(fraudFlags))
            .build();
        
        // Log fraud alert if high risk
        if ("HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel)) {
            mongoLogService.logFraudAlert(
                request.getTransactionReference(),
                request.getUserId(),
                request.getUsername(),
                request.getAmount(),
                fraudScore,
                riskLevel,
                fraudFlags,
                recommendation,
                request.getIpAddress(),
                request.getDeviceFingerprint()
            );
        }
        
        log.info("Fraud check completed: {} - Score: {}, Risk: {}, Recommendation: {}", 
            request.getTransactionReference(), fraudScore, riskLevel, recommendation);
        
        return response;
    }
    
    /**
     * Check for high amount transactions
     */
    private BigDecimal checkHighAmountTransaction(FraudCheckRequest request, List<String> flags) {
        if (request.getAmount().compareTo(highAmountThreshold) > 0) {
            flags.add("HIGH_AMOUNT");
            log.warn("High amount transaction detected: ${}", request.getAmount());
            return new BigDecimal("0.3");  // 30% fraud score contribution
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Check transaction velocity (too many transactions in short time)
     */
    private BigDecimal checkVelocity(FraudCheckRequest request, List<String> flags) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(velocityWindowMinutes);
        
        long recentTransactionCount = transactionRepository
            .countTransactionsInLastHour(request.getUserId(), windowStart);
        
        if (recentTransactionCount >= velocityThresholdCount) {
            flags.add("HIGH_VELOCITY");
            log.warn("High velocity detected for user {}: {} transactions in {} minutes", 
                request.getUserId(), recentTransactionCount, velocityWindowMinutes);
            return new BigDecimal("0.4");  // 40% fraud score contribution
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Check for unusual transaction time (late night/early morning)
     */
    private BigDecimal checkUnusualTime(List<String> flags) {
        int hour = LocalDateTime.now().getHour();
        
        // Transactions between 2 AM and 5 AM are suspicious
        if (hour >= 2 && hour < 5) {
            flags.add("UNUSUAL_TIME");
            log.warn("Transaction at unusual time: {}:00", hour);
            return new BigDecimal("0.2");  // 20% fraud score contribution
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Check for round amounts (e.g., exactly $1000, $5000)
     */
    private BigDecimal checkRoundAmount(FraudCheckRequest request, List<String> flags) {
        BigDecimal amount = request.getAmount();
        
        // Check if amount is a round number (divisible by 1000)
        if (amount.remainder(new BigDecimal("1000")).compareTo(BigDecimal.ZERO) == 0) {
            flags.add("ROUND_AMOUNT");
            log.debug("Round amount detected: ${}", amount);
            return new BigDecimal("0.1");  // 10% fraud score contribution
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Calculate risk level based on fraud score
     */
    private String calculateRiskLevel(BigDecimal fraudScore) {
        if (fraudScore.compareTo(new BigDecimal("0.7")) >= 0) {
            return "CRITICAL";
        } else if (fraudScore.compareTo(new BigDecimal("0.5")) >= 0) {
            return "HIGH";
        } else if (fraudScore.compareTo(new BigDecimal("0.3")) >= 0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * Determine recommendation based on risk
     */
    private String determineRecommendation(BigDecimal fraudScore, String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL" -> "REJECT";
            case "HIGH", "MEDIUM" -> "REVIEW";
            default -> "APPROVE";
        };
    }
    
    /**
     * Build reason message from fraud flags
     */
    private String buildReasonMessage(List<String> flags) {
        if (flags.isEmpty()) {
            return "No fraud indicators detected";
        }
        
        return "Fraud indicators: " + String.join(", ", flags);
    }
    
    /**
     * Block transaction if fraud score is too high
     */
    public void blockIfFraudulent(FraudCheckResponse fraudCheck) {
        if ("REJECT".equals(fraudCheck.getRecommendation())) {
            throw new FraudDetectedException(
                String.format("Transaction blocked due to fraud detection. Risk: %s, Score: %.2f", 
                    fraudCheck.getRiskLevel(), fraudCheck.getFraudScore())
            );
        }
    }
}
