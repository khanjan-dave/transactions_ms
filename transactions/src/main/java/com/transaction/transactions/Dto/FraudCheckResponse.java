package com.transaction.transactions.Dto;
import java.math.BigDecimal;
import java.util.List;

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
public class FraudCheckResponse {
 private String transactionReference;
    private BigDecimal fraudScore;  // 0.0 to 1.0
    private String riskLevel;  // LOW, MEDIUM, HIGH
    private List<String> flags;  // Specific fraud indicators
    private String recommendation;  // APPROVE, REVIEW, REJECT
    private String reason;
}

