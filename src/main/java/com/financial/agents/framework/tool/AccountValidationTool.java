package com.financial.agents.framework.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Tool for validating account details, repayment history, and account validity
 */
@Slf4j
@Component
public class AccountValidationTool {
    
    public AccountValidationResult validateAccount(String accountNumber, String customerId) {
        log.info("Validating account: {} for customer: {}", accountNumber, customerId);
        
        AccountValidationResult result = new AccountValidationResult();
        result.setAccountNumber(accountNumber);
        result.setCustomerId(customerId);
        result.setValid(true);
        result.setActive(true);
        result.setAccountStatus("ACTIVE");
        result.setBalance(new BigDecimal("5000.00"));
        result.setCreditLimit(new BigDecimal("10000.00"));
        result.setRepaymentHistory(getRepaymentHistory(accountNumber));
        result.setRiskMarkers(identifyAccountRiskMarkers(accountNumber));
        result.setAccountAgeMonths(48);
        result.setOverdueAmount(BigDecimal.ZERO);
        result.setPaymentHistoryScore(95); // 0-100, higher is better
        
        return result;
    }
    
    public RepaymentHistory getRepaymentHistory(String accountNumber) {
        log.info("Fetching repayment history for account: {}", accountNumber);
        
        RepaymentHistory history = new RepaymentHistory();
        history.setAccountNumber(accountNumber);
        history.setTotalPayments(48);
        history.setOnTimePayments(46);
        history.setLatePayments(2);
        history.setMissedPayments(0);
        history.setAveragePaymentAmount(new BigDecimal("250.00"));
        history.setLastPaymentDate("2024-01-15");
        history.setLastPaymentAmount(new BigDecimal("300.00"));
        history.setPaymentPattern("CONSISTENT");
        
        return history;
    }
    
    public Map<String, Object> getAccountDetails(String accountNumber) {
        log.info("Fetching account details: {}", accountNumber);
        
        Map<String, Object> details = new HashMap<>();
        details.put("accountNumber", accountNumber);
        details.put("accountType", "CREDIT");
        details.put("status", "ACTIVE");
        details.put("openedDate", "2020-01-15");
        details.put("balance", new BigDecimal("5000.00"));
        details.put("creditLimit", new BigDecimal("10000.00"));
        details.put("availableCredit", new BigDecimal("5000.00"));
        details.put("minimumPayment", new BigDecimal("50.00"));
        details.put("nextPaymentDate", "2024-02-15");
        
        return details;
    }
    
    private Map<String, String> identifyAccountRiskMarkers(String accountNumber) {
        Map<String, String> markers = new HashMap<>();
        // In production, check for:
        // - Overdue payments
        // - High utilization
        // - Frequent late payments
        // - Account restrictions
        // - Fraud flags
        markers.put("overduePayments", "NONE");
        markers.put("highUtilization", "NO");
        markers.put("frequentLatePayments", "NO");
        markers.put("accountRestrictions", "NONE");
        markers.put("fraudFlags", "NONE");
        markers.put("suspiciousActivity", "NONE");
        
        return markers;
    }
    
    public static class AccountValidationResult {
        private String accountNumber;
        private String customerId;
        private boolean valid;
        private boolean active;
        private String accountStatus;
        private BigDecimal balance;
        private BigDecimal creditLimit;
        private RepaymentHistory repaymentHistory;
        private Map<String, String> riskMarkers;
        private int accountAgeMonths;
        private BigDecimal overdueAmount;
        private int paymentHistoryScore;
        
        // Getters and setters
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public String getAccountStatus() { return accountStatus; }
        public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
        
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        
        public BigDecimal getCreditLimit() { return creditLimit; }
        public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
        
        public RepaymentHistory getRepaymentHistory() { return repaymentHistory; }
        public void setRepaymentHistory(RepaymentHistory repaymentHistory) { this.repaymentHistory = repaymentHistory; }
        
        public Map<String, String> getRiskMarkers() { return riskMarkers; }
        public void setRiskMarkers(Map<String, String> riskMarkers) { this.riskMarkers = riskMarkers; }
        
        public int getAccountAgeMonths() { return accountAgeMonths; }
        public void setAccountAgeMonths(int accountAgeMonths) { this.accountAgeMonths = accountAgeMonths; }
        
        public BigDecimal getOverdueAmount() { return overdueAmount; }
        public void setOverdueAmount(BigDecimal overdueAmount) { this.overdueAmount = overdueAmount; }
        
        public int getPaymentHistoryScore() { return paymentHistoryScore; }
        public void setPaymentHistoryScore(int paymentHistoryScore) { this.paymentHistoryScore = paymentHistoryScore; }
    }
    
    public static class RepaymentHistory {
        private String accountNumber;
        private int totalPayments;
        private int onTimePayments;
        private int latePayments;
        private int missedPayments;
        private BigDecimal averagePaymentAmount;
        private String lastPaymentDate;
        private BigDecimal lastPaymentAmount;
        private String paymentPattern;
        
        // Getters and setters
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public int getTotalPayments() { return totalPayments; }
        public void setTotalPayments(int totalPayments) { this.totalPayments = totalPayments; }
        
        public int getOnTimePayments() { return onTimePayments; }
        public void setOnTimePayments(int onTimePayments) { this.onTimePayments = onTimePayments; }
        
        public int getLatePayments() { return latePayments; }
        public void setLatePayments(int latePayments) { this.latePayments = latePayments; }
        
        public int getMissedPayments() { return missedPayments; }
        public void setMissedPayments(int missedPayments) { this.missedPayments = missedPayments; }
        
        public BigDecimal getAveragePaymentAmount() { return averagePaymentAmount; }
        public void setAveragePaymentAmount(BigDecimal averagePaymentAmount) { this.averagePaymentAmount = averagePaymentAmount; }
        
        public String getLastPaymentDate() { return lastPaymentDate; }
        public void setLastPaymentDate(String lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
        
        public BigDecimal getLastPaymentAmount() { return lastPaymentAmount; }
        public void setLastPaymentAmount(BigDecimal lastPaymentAmount) { this.lastPaymentAmount = lastPaymentAmount; }
        
        public String getPaymentPattern() { return paymentPattern; }
        public void setPaymentPattern(String paymentPattern) { this.paymentPattern = paymentPattern; }
    }
}
