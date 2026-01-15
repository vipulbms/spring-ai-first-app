package com.financial.agents.framework.controller;

import com.financial.agents.framework.domain.RefundRequest;
import com.financial.agents.framework.service.RefundOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundController {
    
    private final RefundOrchestrationService orchestrationService;
    
    @PostMapping("/process")
    public ResponseEntity<RefundOrchestrationService.RefundProcessResult> processRefund(
            @Valid @RequestBody RefundRequestDto requestDto) {
        
        log.info("Received refund request: {}", requestDto);
        
        // Convert DTO to domain object
        RefundRequest request = RefundRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .customerId(requestDto.getCustomerId())
            .accountNumber(requestDto.getAccountNumber())
            .refundAmount(requestDto.getRefundAmount())
            .reason(requestDto.getReason())
            .currency(requestDto.getCurrency() != null ? requestDto.getCurrency() : "USD")
            .requestTimestamp(LocalDateTime.now())
            .status(com.financial.agents.framework.domain.RefundStatus.PENDING_REVIEW)
            .customerEmail(requestDto.getCustomerEmail())
            .customerPhone(requestDto.getCustomerPhone())
            .build();
        
        // Process through orchestration
        RefundOrchestrationService.RefundProcessResult result = 
            orchestrationService.processRefund(request);
        
        return ResponseEntity.status(
            result.getStatus() == com.financial.agents.framework.domain.RefundStatus.FULFILLED 
                ? HttpStatus.OK 
                : HttpStatus.BAD_REQUEST
        ).body(result);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Credit Refund Agent Framework is running");
    }
    
    // DTO for request validation
    public static class RefundRequestDto {
        private String customerId;
        private String accountNumber;
        private java.math.BigDecimal refundAmount;
        private String reason;
        private String currency;
        private String customerEmail;
        private String customerPhone;
        
        // Getters and setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public java.math.BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(java.math.BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    }
}
