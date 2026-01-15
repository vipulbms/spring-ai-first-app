package com.financial.agents.framework.tool;

import com.financial.agents.framework.domain.RefundRequest;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for comprehensive risk assessment using LLM reasoning
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskAssessmentTool {
    
    private final ChatLanguageModel chatModel;
    private final CustomerValidationTool customerTool;
    private final CardValidationTool cardTool;
    private final AccountValidationTool accountTool;
    
    public RiskAssessmentResult assessRisk(RefundRequest request, Map<String, Object> validationData) {
        log.info("Assessing risk for request: {}", request.getRequestId());
        
        // Gather all validation data
        Map<String, Object> riskContext = buildRiskContext(request, validationData);
        
        // Use LLM to analyze risk
        String riskAnalysis = performLLMRiskAnalysis(request, riskContext);
        
        // Calculate risk score
        int riskScore = calculateRiskScore(riskContext, riskAnalysis);
        
        // Determine risk level
        String riskLevel = determineRiskLevel(riskScore);
        
        RiskAssessmentResult result = new RiskAssessmentResult();
        result.setRequestId(request.getRequestId());
        result.setRiskScore(riskScore);
        result.setRiskLevel(riskLevel);
        result.setRiskAnalysis(riskAnalysis);
        result.setRiskMarkers(extractRiskMarkers(riskContext));
        result.setRecommendation(generateRecommendation(riskScore, riskLevel));
        
        return result;
    }
    
    private Map<String, Object> buildRiskContext(RefundRequest request, Map<String, Object> validationData) {
        Map<String, Object> context = new HashMap<>();
        
        // Customer validation data
        if (validationData.containsKey("customerValidation")) {
            context.put("customer", validationData.get("customerValidation"));
        }
        
        // Card validation data
        if (validationData.containsKey("cardValidation")) {
            context.put("card", validationData.get("cardValidation"));
        }
        
        // Account validation data
        if (validationData.containsKey("accountValidation")) {
            context.put("account", validationData.get("accountValidation"));
        }
        
        // Request details
        context.put("refundAmount", request.getRefundAmount());
        context.put("reason", request.getReason());
        context.put("currency", request.getCurrency());
        
        return context;
    }
    
    private String performLLMRiskAnalysis(RefundRequest request, Map<String, Object> riskContext) {
        String prompt = buildRiskAnalysisPrompt(request, riskContext);
        
        String systemPrompt = """
            You are a risk assessment expert in a financial institution.
            Analyze the provided data and identify potential risk markers for a credit balance refund request.
            
            Consider:
            1. Customer risk profile (risk score, fraud flags, suspicious activity)
            2. Account status (validity, repayment history, overdue amounts)
            3. Card status (fraud indicators, unusual activity)
            4. Refund amount and reason
            5. Historical patterns and anomalies
            
            Provide a detailed risk analysis with:
            - Identified risk markers
            - Risk level assessment
            - Specific concerns or red flags
            - Recommendations
            
            Be thorough and objective in your analysis.
            """;
        
        String fullPrompt = systemPrompt + "\n\n" + prompt;
        return chatModel.generate(fullPrompt);
    }
    
    private String buildRiskAnalysisPrompt(RefundRequest request, Map<String, Object> riskContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the risk for this refund request:\n\n");
        prompt.append("Request ID: ").append(request.getRequestId()).append("\n");
        prompt.append("Customer ID: ").append(request.getCustomerId()).append("\n");
        prompt.append("Account Number: ").append(request.getAccountNumber()).append("\n");
        prompt.append("Refund Amount: ").append(request.getCurrency()).append(" ").append(request.getRefundAmount()).append("\n");
        prompt.append("Reason: ").append(request.getReason()).append("\n\n");
        
        prompt.append("Validation Data:\n");
        if (riskContext.containsKey("customer")) {
            prompt.append("Customer: ").append(riskContext.get("customer")).append("\n");
        }
        if (riskContext.containsKey("card")) {
            prompt.append("Card: ").append(riskContext.get("card")).append("\n");
        }
        if (riskContext.containsKey("account")) {
            prompt.append("Account: ").append(riskContext.get("account")).append("\n");
        }
        
        prompt.append("\nPlease provide a comprehensive risk analysis.");
        
        return prompt.toString();
    }
    
    private int calculateRiskScore(Map<String, Object> riskContext, String riskAnalysis) {
        int score = 0;
        
        // Base score from customer risk
        if (riskContext.containsKey("customer")) {
            Object customer = riskContext.get("customer");
            if (customer instanceof CustomerValidationTool.CustomerValidationResult) {
                CustomerValidationTool.CustomerValidationResult cust = 
                    (CustomerValidationTool.CustomerValidationResult) customer;
                score += cust.getRiskScore();
            }
        }
        
        // Adjust based on account payment history
        if (riskContext.containsKey("account")) {
            Object account = riskContext.get("account");
            if (account instanceof AccountValidationTool.AccountValidationResult) {
                AccountValidationTool.AccountValidationResult acc = 
                    (AccountValidationTool.AccountValidationResult) account;
                // Lower payment history score increases risk
                score += (100 - acc.getPaymentHistoryScore()) / 2;
                
                // Overdue amounts increase risk
                if (acc.getOverdueAmount().doubleValue() > 0) {
                    score += 20;
                }
            }
        }
        
        // Adjust based on LLM analysis keywords
        String upperAnalysis = riskAnalysis.toUpperCase();
        if (upperAnalysis.contains("HIGH RISK") || upperAnalysis.contains("CRITICAL")) {
            score += 30;
        } else if (upperAnalysis.contains("MODERATE RISK") || upperAnalysis.contains("CONCERN")) {
            score += 15;
        } else if (upperAnalysis.contains("LOW RISK") || upperAnalysis.contains("SAFE")) {
            score -= 10;
        }
        
        // Ensure score is between 0-100
        return Math.max(0, Math.min(100, score));
    }
    
    private String determineRiskLevel(int riskScore) {
        if (riskScore >= 70) {
            return "HIGH";
        } else if (riskScore >= 40) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }
    
    private Map<String, String> extractRiskMarkers(Map<String, Object> riskContext) {
        Map<String, String> markers = new HashMap<>();
        
        if (riskContext.containsKey("customer")) {
            Object customer = riskContext.get("customer");
            if (customer instanceof CustomerValidationTool.CustomerValidationResult) {
                markers.putAll(((CustomerValidationTool.CustomerValidationResult) customer).getRiskMarkers());
            }
        }
        
        if (riskContext.containsKey("account")) {
            Object account = riskContext.get("account");
            if (account instanceof AccountValidationTool.AccountValidationResult) {
                markers.putAll(((AccountValidationTool.AccountValidationResult) account).getRiskMarkers());
            }
        }
        
        if (riskContext.containsKey("card")) {
            Object card = riskContext.get("card");
            if (card instanceof CardValidationTool.CardValidationResult) {
                markers.putAll(((CardValidationTool.CardValidationResult) card).getRiskIndicators());
            }
        }
        
        return markers;
    }
    
    private String generateRecommendation(int riskScore, String riskLevel) {
        if ("HIGH".equals(riskLevel)) {
            return "REJECT - High risk detected. Requires manual review.";
        } else if ("MODERATE".equals(riskLevel)) {
            return "REVIEW - Moderate risk. Additional verification recommended.";
        } else {
            return "APPROVE - Low risk. Standard processing acceptable.";
        }
    }
    
    public static class RiskAssessmentResult {
        private String requestId;
        private int riskScore;
        private String riskLevel;
        private String riskAnalysis;
        private Map<String, String> riskMarkers;
        private String recommendation;
        
        // Getters and setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        
        public String getRiskAnalysis() { return riskAnalysis; }
        public void setRiskAnalysis(String riskAnalysis) { this.riskAnalysis = riskAnalysis; }
        
        public Map<String, String> getRiskMarkers() { return riskMarkers; }
        public void setRiskMarkers(Map<String, String> riskMarkers) { this.riskMarkers = riskMarkers; }
        
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }
}
