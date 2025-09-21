package com.transaction.transactions.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class TransactionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    @Column(name = "sender_username", nullable = false)
    private String senderUsername;
    @Column(name = "sender_account_number", nullable = false)
    private String senderAccountNumber;
    @Column(name = "receiver_username", nullable = false)
    private String receiverUsername;
    @Column(name = "receiver_account_number", nullable = false)
    private String receiverAccountNumber;
    @Column(name = "amount", nullable = false)
    private Double amount;
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    @Column(name = "user_id", nullable = false)
    private Long userId; // Field to store the authenticated user's ID

}