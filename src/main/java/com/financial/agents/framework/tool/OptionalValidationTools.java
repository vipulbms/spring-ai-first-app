package com.financial.agents.framework.tool;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Optional validation tools that LLM can call for additional analysis
 * These are in addition to mandatory validations that run upfront
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OptionalValidationTools {
    
    private final CustomerValidationTool customerTool;
    private final CardValidationTool cardTool;
    private final AccountValidationTool accountTool;
    
    @Tool("Get detailed customer transaction history for the last N months. " +
          "Use this when you need to analyze customer spending patterns or transaction behavior. " +
          "Returns transaction count, average transaction amount, and spending patterns.")
    public String getCustomerTransactionHistory(String customerId, int months) {
        log.info("LLM called: getCustomerTransactionHistory for customer: {}, months: {}", customerId, months);
        
        // In production, this would query transaction database
        Map<String, Object> history = new HashMap<>();
        history.put("customerId", customerId);
        history.put("months", months);
        history.put("totalTransactions", 45);
        history.put("averageTransactionAmount", 125.50);
        history.put("largestTransaction", 500.00);
        history.put("spendingPattern", "CONSISTENT");
        history.put("unusualTransactions", 0);
        history.put("refundCount", 2);
        
        return String.format(
            "Transaction History for Customer %s (Last %d months):\n" +
            "- Total Transactions: %d\n" +
            "- Average Transaction: $%.2f\n" +
            "- Largest Transaction: $%.2f\n" +
            "- Spending Pattern: %s\n" +
            "- Unusual Transactions: %d\n" +
            "- Previous Refunds: %d",
            customerId, months,
            history.get("totalTransactions"),
            history.get("averageTransactionAmount"),
            history.get("largestTransaction"),
            history.get("spendingPattern"),
            history.get("unusualTransactions"),
            history.get("refundCount")
        );
    }
    
    @Tool("Check for similar refund requests in the system. " +
          "Use this to identify patterns, potential fraud, or unusual refund activity. " +
          "Returns count of similar refunds, dates, and amounts.")
    public String checkSimilarRefunds(String accountNumber, int days) {
        log.info("LLM called: checkSimilarRefunds for account: {}, days: {}", accountNumber, days);
        
        // In production, this would query refund history
        Map<String, Object> similar = new HashMap<>();
        similar.put("accountNumber", accountNumber);
        similar.put("days", days);
        similar.put("similarRefundCount", 1);
        similar.put("totalRefundAmount", 250.00);
        similar.put("lastRefundDate", "2024-01-10");
        similar.put("pattern", "NORMAL");
        
        return String.format(
            "Similar Refunds for Account %s (Last %d days):\n" +
            "- Similar Refund Count: %d\n" +
            "- Total Refund Amount: $%.2f\n" +
            "- Last Refund Date: %s\n" +
            "- Pattern: %s",
            accountNumber, days,
            similar.get("similarRefundCount"),
            similar.get("totalRefundAmount"),
            similar.get("lastRefundDate"),
            similar.get("pattern")
        );
    }
    
    @Tool("Get detailed account activity summary including recent payments, charges, and balance changes. " +
          "Use this to understand account behavior and identify any anomalies. " +
          "Returns activity summary with key metrics.")
    public String getAccountActivitySummary(String accountNumber, int days) {
        log.info("LLM called: getAccountActivitySummary for account: {}, days: {}", accountNumber, days);
        
        // In production, this would query account activity
        Map<String, Object> activity = new HashMap<>();
        activity.put("accountNumber", accountNumber);
        activity.put("days", days);
        activity.put("paymentCount", 3);
        activity.put("chargeCount", 8);
        activity.put("balanceChanges", 5);
        activity.put("averageDailyActivity", 2.2);
        activity.put("unusualActivity", "NONE");
        
        return String.format(
            "Account Activity Summary for %s (Last %d days):\n" +
            "- Payments: %d\n" +
            "- Charges: %d\n" +
            "- Balance Changes: %d\n" +
            "- Average Daily Activity: %.1f\n" +
            "- Unusual Activity: %s",
            accountNumber, days,
            activity.get("paymentCount"),
            activity.get("chargeCount"),
            activity.get("balanceChanges"),
            activity.get("averageDailyActivity"),
            activity.get("unusualActivity")
        );
    }
    
    @Tool("Perform deep dive risk analysis on specific risk markers. " +
          "Use this when you need more detailed analysis of specific risk indicators. " +
          "Returns detailed risk analysis for the specified markers.")
    public String analyzeRiskMarkers(String customerId, String accountNumber, String riskMarkerType) {
        log.info("LLM called: analyzeRiskMarkers for customer: {}, account: {}, marker: {}", 
            customerId, accountNumber, riskMarkerType);
        
        // In production, this would perform deep analysis
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("customerId", customerId);
        analysis.put("accountNumber", accountNumber);
        analysis.put("riskMarkerType", riskMarkerType);
        analysis.put("severity", "LOW");
        analysis.put("confidence", 0.85);
        analysis.put("details", "No significant issues detected for " + riskMarkerType);
        analysis.put("recommendation", "Proceed with standard processing");
        
        return String.format(
            "Deep Risk Analysis for %s:\n" +
            "- Customer: %s\n" +
            "- Account: %s\n" +
            "- Marker Type: %s\n" +
            "- Severity: %s\n" +
            "- Confidence: %.0f%%\n" +
            "- Details: %s\n" +
            "- Recommendation: %s",
            riskMarkerType,
            customerId,
            accountNumber,
            riskMarkerType,
            analysis.get("severity"),
            ((Double) analysis.get("confidence")) * 100,
            analysis.get("details"),
            analysis.get("recommendation")
        );
    }
    
    @Tool("Get customer relationship history including account age, product usage, and service interactions. " +
          "Use this to assess customer loyalty and relationship quality. " +
          "Returns relationship metrics and history.")
    public String getCustomerRelationshipHistory(String customerId) {
        log.info("LLM called: getCustomerRelationshipHistory for customer: {}", customerId);
        
        // In production, this would query CRM system
        Map<String, Object> relationship = new HashMap<>();
        relationship.put("customerId", customerId);
        relationship.put("accountAgeMonths", 48);
        relationship.put("productCount", 3);
        relationship.put("serviceInteractions", 12);
        relationship.put("satisfactionScore", 4.5);
        relationship.put("loyaltyTier", "GOLD");
        relationship.put("lastInteraction", "2024-01-15");
        
        return String.format(
            "Customer Relationship History for %s:\n" +
            "- Account Age: %d months\n" +
            "- Products: %d\n" +
            "- Service Interactions: %d\n" +
            "- Satisfaction Score: %.1f/5.0\n" +
            "- Loyalty Tier: %s\n" +
            "- Last Interaction: %s",
            customerId,
            relationship.get("accountAgeMonths"),
            relationship.get("productCount"),
            relationship.get("serviceInteractions"),
            relationship.get("satisfactionScore"),
            relationship.get("loyaltyTier"),
            relationship.get("lastInteraction")
        );
    }
    
    @Tool("Validate specific card details and check for recent fraud reports. " +
          "Use this when you need additional card validation beyond the initial check. " +
          "Returns detailed card validation results.")
    public String validateCardDetails(String accountNumber, String cardNumber) {
        log.info("LLM called: validateCardDetails for account: {}", accountNumber);
        
        CardValidationTool.CardValidationResult result = 
            cardTool.validateCard(accountNumber, cardNumber);
        
        return String.format(
            "Card Validation Details:\n" +
            "- Card Valid: %s\n" +
            "- Card Active: %s\n" +
            "- Card Expired: %s\n" +
            "- Card Type: %s\n" +
            "- Recent Transactions: %d\n" +
            "- Risk Indicators: %s",
            result.isValid(),
            result.isActive(),
            result.isExpired(),
            result.getCardType(),
            result.getRecentTransactions(),
            result.getRiskIndicators()
        );
    }
    
    @Tool("Get comprehensive repayment history analysis with trends and patterns. " +
          "Use this when you need deeper analysis of payment behavior. " +
          "Returns detailed repayment analysis with trends.")
    public String getDetailedRepaymentAnalysis(String accountNumber) {
        log.info("LLM called: getDetailedRepaymentAnalysis for account: {}", accountNumber);
        
        AccountValidationTool.RepaymentHistory history = 
            accountTool.getRepaymentHistory(accountNumber);
        
        double onTimePercentage = (double) history.getOnTimePayments() / history.getTotalPayments() * 100;
        
        return String.format(
            "Detailed Repayment Analysis for Account %s:\n" +
            "- Total Payments: %d\n" +
            "- On-Time Payments: %d (%.1f%%)\n" +
            "- Late Payments: %d\n" +
            "- Missed Payments: %d\n" +
            "- Average Payment: $%.2f\n" +
            "- Last Payment: %s ($%.2f)\n" +
            "- Payment Pattern: %s",
            accountNumber,
            history.getTotalPayments(),
            history.getOnTimePayments(),
            onTimePercentage,
            history.getLatePayments(),
            history.getMissedPayments(),
            history.getAveragePaymentAmount(),
            history.getLastPaymentDate(),
            history.getLastPaymentAmount(),
            history.getPaymentPattern()
        );
    }
}
