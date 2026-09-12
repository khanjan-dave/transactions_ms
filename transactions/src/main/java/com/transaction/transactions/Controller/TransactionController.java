package com.transaction.transactions.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transaction.transactions.Dto.TransactionRequest;
import com.transaction.transactions.Dto.TransactionResponse;
import com.transaction.transactions.Services.JwtService;
import com.transaction.transactions.Services.TransactionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/transactions")
@Slf4j
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JwtService jwtService;

    /**
     * Create new transaction
     * POST /api/transactions/create
     */
    @PostMapping("/create")
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader(name = "Authorization") String authHeader,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        
        try {
            // Extract JWT token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                error.put("message", "No token provided");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String jwt = authHeader.substring(7);
            
            // Extract user info from token
            String username = jwtService.extractUsername(jwt);
            Long userId = jwtService.extractUserId(jwt);

            if (userId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                error.put("message", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Get IP address and User Agent
            String ipAddress = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            log.info("Transaction request received from user: {} (ID: {})", username, userId);
            
            // Create transaction
            TransactionResponse response = transactionService.createTransaction(
                userId, username, request, ipAddress, userAgent
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error creating transaction: {}", e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getClass().getSimpleName());
            error.put("message", e.getMessage());
            
            // Return appropriate status code based on exception type
            HttpStatus status = determineHttpStatus(e);
            
            return ResponseEntity.status(status).body(error);
        }
    }

    /**
     * Get transaction by reference
     * GET /api/transactions/{reference}
     */
    @GetMapping("/{reference}")
    public ResponseEntity<?> getTransaction(@PathVariable String reference) {
        try {
            TransactionResponse transaction = transactionService
                .getTransactionByReference(reference);
            
            return ResponseEntity.ok(transaction);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not Found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Get all transactions for authenticated user
     * GET /api/transactions/my-transactions
     */
    @GetMapping("/my-transactions")
    public ResponseEntity<?> getMyTransactions(
            @RequestHeader(name = "Authorization") String authHeader) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                error.put("message", "No token provided");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String jwt = authHeader.substring(7);
            Long userId = jwtService.extractUserId(jwt);

            if (userId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                error.put("message", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<TransactionResponse> transactions = transactionService
                .getUserTransactions(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", transactions.size());
            response.put("transactions", transactions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get sent transactions (where user is sender)
     * GET /api/transactions/sent
     */
    @GetMapping("/sent")
    public ResponseEntity<?> getSentTransactions(
            @RequestHeader(name = "Authorization") String authHeader) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                error.put("message", "No token provided");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String jwt = authHeader.substring(7);
            Long userId = jwtService.extractUserId(jwt);

            if (userId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                error.put("message", "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<TransactionResponse> transactions = transactionService
                .getSenderTransactions(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", transactions.size());
            response.put("transactions", transactions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Health check endpoint
     * GET /api/transactions/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Transaction Management");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Extract client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Determine HTTP status based on exception type
     */
    private HttpStatus determineHttpStatus(Exception e) {
        String exceptionName = e.getClass().getSimpleName();

        return switch (exceptionName) {
            case "InsufficientBalanceException", "InvalidTransactionException" -> HttpStatus.BAD_REQUEST;
            case "TransactionLimitExceededException", "FraudDetectedException" -> HttpStatus.FORBIDDEN;
            case "RateLimitExceededException" -> HttpStatus.TOO_MANY_REQUESTS;
            case "DuplicateTransactionException" -> HttpStatus.CONFLICT;
            case "AccountNotFoundException" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}