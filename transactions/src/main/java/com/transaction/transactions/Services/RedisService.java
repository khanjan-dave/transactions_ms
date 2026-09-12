package com.transaction.transactions.Services;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Set value with expiration
     */
    public void setValue(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Redis SET: {} = {}", key, value);
        } catch (Exception e) {
            log.error("Redis SET failed for key: {}", key, e);
        }
    }
    
    /**
     * Get value
     */
    public Object getValue(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Redis GET: {} = {}", key, value);
            return value;
        } catch (Exception e) {
            log.error("Redis GET failed for key: {}", key, e);
            return null;
        }
    }
    
    /**
     * Increment value (for counters)
     */
    public Long increment(String key) {
        try {
            Long value = redisTemplate.opsForValue().increment(key);
            log.debug("Redis INCREMENT: {} = {}", key, value);
            return value;
        } catch (Exception e) {
            log.error("Redis INCREMENT failed for key: {}", key, e);
            return null;
        }
    }
    
    /**
     * Increment with expiration
     */
    public Long incrementWithExpiry(String key, long timeout, TimeUnit unit) {
        try {
            Long value = redisTemplate.opsForValue().increment(key);
            if (value != null && value == 1) {
                // First increment, set expiration
                redisTemplate.expire(key, timeout, unit);
            }
            log.debug("Redis INCREMENT with expiry: {} = {}", key, value);
            return value;
        } catch (Exception e) {
            log.error("Redis INCREMENT with expiry failed for key: {}", key, e);
            return null;
        }
    }
    
    /**
     * Delete key
     */
    public Boolean deleteKey(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("Redis DELETE: {} = {}", key, deleted);
            return deleted;
        } catch (Exception e) {
            log.error("Redis DELETE failed for key: {}", key, e);
            return false;
        }
    }
    
    /**
     * Check if key exists
     */
    public Boolean hasKey(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            log.debug("Redis EXISTS: {} = {}", key, exists);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Redis EXISTS failed for key: {}", key, e);
            return false;
        }
    }
    
    /**
     * Set expiration on existing key
     */
    public Boolean setExpire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            log.debug("Redis EXPIRE: {} = {} {}", key, timeout, unit);
            return result;
        } catch (Exception e) {
            log.error("Redis EXPIRE failed for key: {}", key, e);
            return false;
        }
    }
    
    /**
     * Get remaining TTL
     */
    public Long getExpire(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            log.debug("Redis TTL: {} = {} seconds", key, ttl);
            return ttl;
        } catch (Exception e) {
            log.error("Redis TTL failed for key: {}", key, e);
            return null;
        }
    }
}