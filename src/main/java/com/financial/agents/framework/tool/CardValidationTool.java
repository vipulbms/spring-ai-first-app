package com.financial.agents.framework.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for validating card details and checking for risk markers
 */
@Slf4j
@Component
public class CardValidationTool {
    
    public CardValidationResult validateCard(String accountNumber, String cardNumber) {
        log.info("Validating card for account: {}, card: {}", accountNumber, maskCardNumber(cardNumber));
        
        CardValidationResult result = new CardValidationResult();
        result.setCardNumber(maskCardNumber(cardNumber));
        result.setValid(true);
        result.setActive(true);
        result.setExpired(false);
        result.setCardType("CREDIT");
        result.setRiskIndicators(checkCardRiskIndicators(accountNumber, cardNumber));
        result.setRecentTransactions(getRecentTransactionCount(accountNumber));
        
        return result;
    }
    
    public Map<String, Object> getCardDetails(String accountNumber) {
        log.info("Fetching card details for account: {}", accountNumber);
        
        Map<String, Object> details = new HashMap<>();
        details.put("accountNumber", accountNumber);
        details.put("cardType", "CREDIT");
        details.put("cardStatus", "ACTIVE");
        details.put("creditLimit", 10000.00);
        details.put("availableCredit", 7500.00);
        details.put("expiryDate", "2025-12-31");
        details.put("cardHolderName", "John Doe");
        
        return details;
    }
    
    private Map<String, String> checkCardRiskIndicators(String accountNumber, String cardNumber) {
        Map<String, String> indicators = new HashMap<>();
        // In production, check for:
        // - Stolen/lost card reports
        // - Fraud patterns
        // - Unusual spending patterns
        // - Geographic anomalies
        indicators.put("stolenReported", "NO");
        indicators.put("fraudPatterns", "NONE");
        indicators.put("unusualActivity", "NONE");
        indicators.put("geographicAnomaly", "NO");
        return indicators;
    }
    
    private int getRecentTransactionCount(String accountNumber) {
        // Simulate recent transaction count
        // In production, query transaction database
        return 15; // Last 30 days
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
    
    public static class CardValidationResult {
        private String cardNumber;
        private boolean valid;
        private boolean active;
        private boolean expired;
        private String cardType;
        private Map<String, String> riskIndicators;
        private int recentTransactions;
        
        // Getters and setters
        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public boolean isExpired() { return expired; }
        public void setExpired(boolean expired) { this.expired = expired; }
        
        public String getCardType() { return cardType; }
        public void setCardType(String cardType) { this.cardType = cardType; }
        
        public Map<String, String> getRiskIndicators() { return riskIndicators; }
        public void setRiskIndicators(Map<String, String> riskIndicators) { this.riskIndicators = riskIndicators; }
        
        public int getRecentTransactions() { return recentTransactions; }
        public void setRecentTransactions(int recentTransactions) { this.recentTransactions = recentTransactions; }
    }
}
