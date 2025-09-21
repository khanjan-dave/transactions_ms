package com.transaction.transactions.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private String senderUsername;
    private String senderAccountNumber;
    private String receiverUsername;
    private String receiverAccountNumber;
    private Double amount;
    private String transactionType;
}
