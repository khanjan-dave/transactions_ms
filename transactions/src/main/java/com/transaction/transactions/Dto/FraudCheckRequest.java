package com.transaction.transactions.Dto;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudCheckRequest {
private String transactionReference;
    private Long userId;
    private String username;
    private String accountNumber;
    private BigDecimal amount;
    private String transactionType;
    private String ipAddress;
    private String userAgent;
    private String deviceFingerprint;
}