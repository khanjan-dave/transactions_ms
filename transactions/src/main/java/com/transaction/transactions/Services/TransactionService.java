package com.transaction.transactions.Services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.transaction.transactions.Dto.TransactionRequest;
import com.transaction.transactions.Event.TransactionEvent;
import com.transaction.transactions.Model.TransactionModel;
import com.transaction.transactions.Repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final KafkaProducerService kafkaProducerService;

    public void createTransaction(Long userId, TransactionRequest request) {
        TransactionModel transaction = new TransactionModel();
        transaction.setUserId(userId);
        transaction.setSenderUsername(request.getSenderUsername());
        transaction.setSenderAccountNumber(request.getSenderAccountNumber());
        transaction.setReceiverUsername(request.getReceiverUsername());
        transaction.setReceiverAccountNumber(request.getReceiverAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(transaction);

        TransactionEvent event = new TransactionEvent();
        event.setTransactionId(transaction.getId());
        event.setSenderUsername(transaction.getSenderUsername());
        event.setSenderAccountNumber(transaction.getSenderAccountNumber());
        event.setReceiverUsername(transaction.getReceiverUsername());
        event.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        event.setAmount(transaction.getAmount());
        event.setTransactionType(transaction.getTransactionType());
        
        kafkaProducerService.sendMessage(event);
    }
}