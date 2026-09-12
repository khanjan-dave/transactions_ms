package com.transaction.transactions.Services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transaction.transactions.Dto.FraudCheckRequest;
import com.transaction.transactions.Dto.FraudCheckResponse;
import com.transaction.transactions.Dto.TransactionRequest;
import com.transaction.transactions.Dto.TransactionResponse;
import com.transaction.transactions.Event.TransactionEvent;
import com.transaction.transactions.Exception.DuplicateTransactionException;
import com.transaction.transactions.Model.TransactionModel;
import com.transaction.transactions.Model.TransactionStatus;
import com.transaction.transactions.Model.TransactionType;
import com.transaction.transactions.Repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;
    
    @Autowired
    private TransactionValidationService validationService;
    
    @Autowired
    private FraudDetectionService fraudDetectionService;
    
    @Autowired
    private RateLimitService rateLimitService;
    
    @Autowired
    private MongoLogService mongoLogService;
    
    @Autowired
    private TransactionAuditService auditService;
    
    /**
     * Create new transaction (Main business logic)
     */
    @Transactional
public TransactionResponse createTransaction(
        Long userId, 
        String username,
        TransactionRequest request,
        String ipAddress,
        String userAgent) {
    
    String transactionReference = generateTransactionReference();
    
    log.info("Creating transaction: {} for user: {}", transactionReference, userId);
    
    try {
        // STEP 1: Rate Limiting Check
        rateLimitService.checkRateLimit(userId);
        
        // STEP 2: Idempotency Check
        if (request.getIdempotencyKey() != null) {
            rateLimitService.checkDuplicateTransaction(request.getIdempotencyKey());
        }
        
        if (transactionRepository.existsByTransactionReference(transactionReference)) {
            throw new DuplicateTransactionException(
                "Transaction reference already exists: " + transactionReference);
        }
        
        // STEP 3: Validate Transaction
        validationService.validateTransaction(request, userId);
        validationService.validateAccountNumber(request.getSenderAccountNumber());
        
        // Log: Transaction Initiated (MongoDB only - async)
        mongoLogService.logTransactionActivity(
            transactionReference, userId, username, "INITIATED",
            request.getTransactionType().name(), request.getAmount(),
            "PENDING", ipAddress, userAgent, request.getDeviceFingerprint()
        );
        
        log.info("Transaction validation passed for user {}", userId);
        
        // STEP 4: Fraud Detection
        FraudCheckRequest fraudRequest = buildFraudCheckRequest(
            transactionReference, userId, username, request, ipAddress, userAgent
        );
        
        FraudCheckResponse fraudCheck = fraudDetectionService.checkFraud(fraudRequest);
        
        // Block if fraud detected
        fraudDetectionService.blockIfFraudulent(fraudCheck);
        
        // Log: Fraud Check Passed (MongoDB only)
        mongoLogService.logTransactionActivity(
            transactionReference, userId, username, "FRAUD_CHECK_PASSED",
            request.getTransactionType().name(), request.getAmount(),
            "VALIDATING", ipAddress, userAgent, request.getDeviceFingerprint()
        );
        
        // STEP 5: Create Transaction Record
        TransactionModel transaction = buildTransactionModel(
            transactionReference, userId, username, request, 
            fraudCheck, ipAddress, userAgent
        );
        
        transaction.setStatus(TransactionStatus.PENDING);
        
        // SAVE TRANSACTION FIRST - This gives it an ID!
        transaction = transactionRepository.save(transaction);
        
        log.info("Transaction saved to database: {}", transactionReference);
        
        // NOW we can log audit with the transaction ID
        auditService.logAudit(
            transaction.getId(), transactionReference, "TRANSACTION_CREATED", "SUCCESS",
            "Transaction created and saved to database", ipAddress, userAgent
        );
        
        // STEP 6: Send to Kafka for processing
        TransactionEvent event = buildTransactionEvent(transaction);
        kafkaProducerService.sendMessage(event);
        
        log.info("Transaction event sent to Kafka: {}", transactionReference);
        
        // Log: Processing Started (MongoDB only)
        mongoLogService.logTransactionActivity(
            transactionReference, userId, username, "PROCESSING_STARTED",
            request.getTransactionType().name(), request.getAmount(),
            "PROCESSING", ipAddress, userAgent, request.getDeviceFingerprint()
        );
        
        // STEP 7: Build Response
        TransactionResponse response = buildTransactionResponse(transaction, fraudCheck);
        
        log.info("Transaction created successfully: {}", transactionReference);
        
        return response;
        
    } catch (Exception e) {
        // Log failure to MongoDB
        mongoLogService.logFailedTransaction(
            transactionReference, userId, username, 
            request.getSenderAccountNumber(), request.getAmount(),
            request.getTransactionType().name(), e.getClass().getSimpleName(),
            "TXNERR001", e.getMessage(), ipAddress, userAgent
        );
        
        // DON'T try to log audit here - transaction might not exist!
        
        log.error("Transaction failed: {} - Error: {}", transactionReference, e.getMessage());
        
        throw e;
    }
}
    /**
     * Get transaction by reference
     */
    public TransactionResponse getTransactionByReference(String transactionReference) {
        TransactionModel transaction = transactionRepository
            .findByTransactionReference(transactionReference)
            .orElseThrow(() -> new RuntimeException(
                "Transaction not found: " + transactionReference));
        
        return buildTransactionResponse(transaction, null);
    }
    
    /**
     * Get all transactions for a user
     */
    public List<TransactionResponse> getUserTransactions(Long userId) {
        List<TransactionModel> transactions = transactionRepository
            .findByUserId(userId);
        
        return transactions.stream()
            .map(txn -> buildTransactionResponse(txn, null))
            .collect(Collectors.toList());
    }
    
    /**
     * Get transaction history for sender
     */
    public List<TransactionResponse> getSenderTransactions(Long userId) {
        List<TransactionModel> transactions = transactionRepository
            .findBySenderUserIdOrderByInitiatedAtDesc(userId);
        
        return transactions.stream()
            .map(txn -> buildTransactionResponse(txn, null))
            .collect(Collectors.toList());
    }
    
    /**
     * Update transaction status (called by Kafka consumer or Account MS)
     */
    @Transactional
    public void updateTransactionStatus(
            String transactionReference, 
            TransactionStatus status,
            String reason) {
        
        TransactionModel transaction = transactionRepository
            .findByTransactionReference(transactionReference)
            .orElseThrow(() -> new RuntimeException(
                "Transaction not found: " + transactionReference));
        
        transaction.setStatus(status);
        
        if (status == TransactionStatus.COMPLETED) {
            transaction.setCompletedAt(LocalDateTime.now());
        } else if (status == TransactionStatus.FAILED) {
            transaction.setFailedAt(LocalDateTime.now());
        }
        
        transactionRepository.save(transaction);
        
        log.info("Transaction status updated: {} -> {}", transactionReference, status);
        
        // Audit log
        auditService.logAudit(
            transaction.getId(), transactionReference, 
            "STATUS_UPDATED", "SUCCESS",
            String.format("Status changed to %s. Reason: %s", status, reason),
            null, null
        );
    }

    /**
     * Generate unique transaction reference
     * Format: TXN20260309123456789
     */
    private String generateTransactionReference() {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%05d", new Random().nextInt(100000));
        return "TXN" + timestamp + random;
    }

    /**
     * Build TransactionModel from request
     */
    private TransactionModel buildTransactionModel(
            String transactionReference,
            Long userId,
            String username,
            TransactionRequest request,
            FraudCheckResponse fraudCheck,
            String ipAddress,
            String userAgent) {
        
        TransactionModel transaction = new TransactionModel();
        
        // Transaction identifiers
        transaction.setTransactionReference(transactionReference);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency("USD");
        
        // Sender information
        transaction.setSenderUserId(userId);
        transaction.setSenderUsername(username);
        transaction.setSenderAccountNumber(request.getSenderAccountNumber());
        
        // Receiver information
        // transaction.setReceiverUsername(request.getReceiverUsername());
        // transaction.setReceiverAccountNumber(request.getReceiverAccountNumber());

        // Receiver information - LOGIC BASED ON TRANSACTION TYPE
        if (request.getTransactionType() == TransactionType.DEPOSIT) {
            // For DEPOSIT: receiver is the same as sender
            transaction.setReceiverUserId(userId);
            transaction.setReceiverUsername(username);
            transaction.setReceiverAccountNumber(request.getSenderAccountNumber());
        } else if (request.getTransactionType() == TransactionType.WITHDRAWAL) {
            // For WITHDRAWAL: no receiver (funds leaving the system)
            transaction.setReceiverUserId(null);
            transaction.setReceiverUsername(null);
            transaction.setReceiverAccountNumber(null);
        } else if (request.getTransactionType() == TransactionType.TRANSFER || 
                request.getTransactionType() == TransactionType.PAYMENT) {
            // For TRANSFER/PAYMENT: use provided receiver info
            transaction.setReceiverUsername(request.getReceiverUsername());
            transaction.setReceiverAccountNumber(request.getReceiverAccountNumber());
            // Note: receiverUserId would need to be looked up from Account MS
        }
        
        // Metadata
        transaction.setDescription(request.getDescription());
        transaction.setNotes(request.getNotes());
        
        // Security & Audit
        transaction.setIpAddress(ipAddress);
        transaction.setUserAgent(userAgent);
        transaction.setDeviceFingerprint(request.getDeviceFingerprint());
        
        // Fraud detection
        transaction.setFraudScore(fraudCheck.getFraudScore());
        transaction.setFraudFlags(String.join(", ", fraudCheck.getFlags()));
        
        // Timestamps
        transaction.setInitiatedAt(LocalDateTime.now());
        
        return transaction;
    }

    /**
     * Build FraudCheckRequest
     */
    private FraudCheckRequest buildFraudCheckRequest(
            String transactionReference,
            Long userId,
            String username,
            TransactionRequest request,
            String ipAddress,
            String userAgent) {
        
        return FraudCheckRequest.builder()
            .transactionReference(transactionReference)
            .userId(userId)
            .username(username)
            .accountNumber(request.getSenderAccountNumber())
            .amount(request.getAmount())
            .transactionType(request.getTransactionType().name())
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .deviceFingerprint(request.getDeviceFingerprint())
            .build();
    }

    /**
     * Build TransactionEvent for Kafka
     */
    private TransactionEvent buildTransactionEvent(TransactionModel transaction) {
        TransactionEvent event = new TransactionEvent();
        event.setTransactionId(transaction.getId());
        event.setSenderUsername(transaction.getSenderUsername());
        event.setSenderAccountNumber(transaction.getSenderAccountNumber());
        // event.setReceiverUsername(transaction.getReceiverUsername());

        // event.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        // Set receiver based on transaction type
        if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
            // For DEPOSIT: receiver is same as sender
            event.setReceiverUsername(transaction.getSenderUsername());
            event.setReceiverAccountNumber(transaction.getSenderAccountNumber());
        } else if (transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
            // For WITHDRAWAL: no receiver
            event.setReceiverUsername(null);
            event.setReceiverAccountNumber(null);
        } else {
            // For TRANSFER/PAYMENT: use actual receiver
            event.setReceiverUsername(transaction.getReceiverUsername());
            event.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        }
        
        event.setAmount(transaction.getAmount().doubleValue());
        event.setTransactionType(transaction.getTransactionType().name());
        return event;
    }

    /**
     * Build TransactionResponse
     */
    private TransactionResponse buildTransactionResponse(
            TransactionModel transaction,
            FraudCheckResponse fraudCheck) {
        
        return TransactionResponse.builder()
            .id(transaction.getId())
            .transactionReference(transaction.getTransactionReference())
            .transactionType(transaction.getTransactionType())
            .amount(transaction.getAmount())
            .currency(transaction.getCurrency())
            .status(transaction.getStatus())
            .senderUsername(transaction.getSenderUsername())
            .senderAccountNumberMasked(maskAccountNumber(transaction.getSenderAccountNumber()))
            .senderBalanceAfter(transaction.getSenderBalanceAfter())
            .receiverUsername(transaction.getReceiverUsername())
            .receiverAccountNumberMasked(
                transaction.getReceiverAccountNumber() != null 
                    ? maskAccountNumber(transaction.getReceiverAccountNumber()) 
                    : null
            )
            .description(transaction.getDescription())
            .initiatedAt(transaction.getInitiatedAt())
            .completedAt(transaction.getCompletedAt())
            .fraudScore(transaction.getFraudScore())
            .riskLevel(fraudCheck != null ? fraudCheck.getRiskLevel() : 
                calculateRiskLevel(transaction.getFraudScore()))
            .message("Transaction created successfully and is being processed")
            .build();
    }

    /**
     * Mask account number (show last 4 digits)
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        int length = accountNumber.length();
        return "****" + accountNumber.substring(length - 4);
    }

    /**
     * Calculate risk level from fraud score
     */
    private String calculateRiskLevel(BigDecimal fraudScore) {
        if (fraudScore == null) {
            return "LOW";
        }
        
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
}