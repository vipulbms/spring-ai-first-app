package com.financial.agents.framework.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private String requestId;
    private String customerId;
    private String accountNumber;
    private BigDecimal refundAmount;
    private String reason;
    private String currency;
    private LocalDateTime requestTimestamp;
    private RefundStatus status;
    private String customerEmail;
    private String customerPhone;
}
