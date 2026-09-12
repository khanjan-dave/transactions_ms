package com.transaction.transactions.Services;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.transaction.transactions.Exception.RateLimitExceededException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RateLimitService {
    
    @Autowired
    private RedisService redisService;
    
    @Value("${transaction.rate.limit.per.minute:5}")
    private int rateLimitPerMinute;
    
    @Value("${transaction.rate.limit.per.hour:20}")
    private int rateLimitPerHour;
    
    /**
     * Check if user has exceeded rate limit
     */
    public void checkRateLimit(Long userId) {
        
        // Check per-minute limit
        String minuteKey = "rate:limit:minute:user:" + userId;
        Long minuteCount = redisService.incrementWithExpiry(minuteKey, 1, TimeUnit.MINUTES);
        
        if (minuteCount != null && minuteCount > rateLimitPerMinute) {
            log.warn("User {} exceeded per-minute rate limit: {}/{}", 
                userId, minuteCount, rateLimitPerMinute);
            throw new RateLimitExceededException(
                String.format("Too many transactions. You can make %d transactions per minute. Please wait.", 
                    rateLimitPerMinute)
            );
        }
        
        // Check per-hour limit
        String hourKey = "rate:limit:hour:user:" + userId;
        Long hourCount = redisService.incrementWithExpiry(hourKey, 1, TimeUnit.HOURS);
        
        if (hourCount != null && hourCount > rateLimitPerHour) {
            log.warn("User {} exceeded per-hour rate limit: {}/{}", 
                userId, hourCount, rateLimitPerHour);
            throw new RateLimitExceededException(
                String.format("Too many transactions. You can make %d transactions per hour. Please try again later.", 
                    rateLimitPerHour)
            );
        }
        
        log.info("Rate limit check passed for user {}: minute={}/{}, hour={}/{}", 
            userId, minuteCount, rateLimitPerMinute, hourCount, rateLimitPerHour);
    }
    
    /**
     * Check duplicate transaction using idempotency key
     */
    public void checkDuplicateTransaction(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return;
        }
        
        String key = "idempotency:" + idempotencyKey;
        
        if (redisService.hasKey(key)) {
            log.warn("Duplicate transaction detected: {}", idempotencyKey);
            throw new com.transaction.transactions.Exception.DuplicateTransactionException(
                "Duplicate transaction detected. This transaction was already processed."
            );
        }
        
        // Store idempotency key for 5 minutes
        redisService.setValue(key, "processed", 5, TimeUnit.MINUTES);
    }
    
    /**
     * Reset rate limit for user (admin function)
     */
    public void resetRateLimit(Long userId) {
        String minuteKey = "rate:limit:minute:user:" + userId;
        String hourKey = "rate:limit:hour:user:" + userId;
        
        redisService.deleteKey(minuteKey);
        redisService.deleteKey(hourKey);
        
        log.info("Rate limit reset for user {}", userId);
    }
}
