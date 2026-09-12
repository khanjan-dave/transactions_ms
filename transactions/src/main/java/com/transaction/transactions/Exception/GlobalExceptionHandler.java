package com.transaction.transactions.Exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(
            InsufficientBalanceException ex, WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Insufficient Balance");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateTransaction(
            DuplicateTransactionException ex, WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Duplicate Transaction");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    @ExceptionHandler(TransactionLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionLimitExceeded(
            TransactionLimitExceededException ex, WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Transaction Limit Exceeded");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransaction(
            InvalidTransactionException ex, WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Invalid Transaction");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(FraudDetectedException.class)
    public ResponseEntity<Map<String, Object>> handleFraudDetected(
            FraudDetectedException ex, WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Fraud Detected");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitExceeded(
            RateLimitExceededException ex, WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("error", "Rate Limit Exceeded");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
    
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNotFound(
            AccountNotFoundException ex, WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Account Not Found");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        // Log the exception for debugging
        logger.error("An unexpected error occurred", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}