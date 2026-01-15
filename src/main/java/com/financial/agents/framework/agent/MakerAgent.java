package com.financial.agents.framework.agent;

import com.financial.agents.framework.domain.AgentResponse;
import com.financial.agents.framework.domain.AgentType;
import com.financial.agents.framework.domain.RefundRequest;
import com.financial.agents.framework.memory.MemoryService;
import com.financial.agents.framework.service.AgentToolService;
import com.financial.agents.framework.service.AuditService;
import com.financial.agents.framework.service.ContextService;
import com.financial.agents.framework.tool.RiskAssessmentTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MakerAgent extends BaseAgent {
    
    private static final String AGENT_NAME = "MakerAgent";
    private static final BigDecimal MAX_REFUND_AMOUNT = new BigDecimal("10000.00");
    
    private final ContextService contextService;
    
    public MakerAgent(ChatLanguageModel chatModel, AuditService auditService, 
                     MemoryService memoryService, AgentToolService agentToolService,
                     ContextService contextService) {
        super(chatModel, auditService, memoryService, agentToolService, AGENT_NAME, AgentType.MAKER);
        this.contextService = contextService;
    }
    
    @Override
    protected boolean enableOptionalTools() {
        return true; // Enable optional tool calling for MakerAgent
    }
    
    @Override
    protected String getSystemPrompt() {
        return """
            You are a Maker Agent in a financial institution responsible for reviewing credit balance refund requests.
            Your role is to:
            1. Validate customer information and account details using validation tools
            2. Check risk markers and repayment history
            3. Verify the refund amount is reasonable and within policy limits
            4. Check if the reason for refund is valid
            5. Ensure all required information is present
            6. Make an initial decision: APPROVE or REJECT
            
            Guidelines:
            - Maximum refund amount is $10,000.00
            - Valid reasons include: overpayment, duplicate charge, service cancellation, account closure
            - All customer information must be complete and validated
            - Account must be in good standing with good repayment history
            - Consider risk markers: high risk scores, overdue amounts, fraud flags, suspicious activity
            - Review payment history: late payments, missed payments, payment patterns
            
            You will receive validation data including:
            - Customer validation (risk score, fraud flags, account status)
            - Account validation (repayment history, overdue amounts, account validity)
            - Card validation (fraud indicators, unusual activity)
            - Risk assessment (comprehensive risk analysis with LLM reasoning)
            
            OPTIONAL TOOLS AVAILABLE:
            If you need additional information, you can use these optional tools:
            - getCustomerTransactionHistory: Get detailed transaction history for analysis
            - checkSimilarRefunds: Check for similar refund patterns
            - getAccountActivitySummary: Get account activity details
            - analyzeRiskMarkers: Perform deep dive risk analysis
            - getCustomerRelationshipHistory: Get customer relationship metrics
            - validateCardDetails: Get additional card validation details
            - getDetailedRepaymentAnalysis: Get comprehensive repayment analysis
            
            Use these tools when you need more information to make an informed decision.
            The mandatory validations have already been performed, but you can call these tools
            for additional analysis if needed.
            
            Provide a clear reasoning for your decision. If you approve, explain why. If you reject, explain what is missing or invalid.
            Format your response as: DECISION: [APPROVE/REJECT] REASONING: [Your detailed reasoning including risk considerations]
            """;
    }
    
    @Override
    protected AgentResponse processRequest(RefundRequest request, Map<String, Object> context) {
        log.info("Maker agent reviewing request: {}", request.getRequestId());
        
        // Initial validation
        if (!validateRequest(request)) {
            String reasoning = generateReasoning(request, "Request validation failed. Missing required information.");
            return createResponse(request, false, reasoning, createMetadata("VALIDATION_FAILED"));
        }
        
        // Build comprehensive context with all validations
        Map<String, Object> fullContext = contextService.buildContext(request);
        context.putAll(fullContext);
        
        // Get validation data for LLM reasoning
        Map<String, Object> validationData = contextService.getValidationData(fullContext);
        
        // Check amount limits
        if (request.getRefundAmount().compareTo(MAX_REFUND_AMOUNT) > 0) {
            String reasoning = generateReasoning(request, 
                String.format("Refund amount $%.2f exceeds maximum limit of $%.2f", 
                    request.getRefundAmount(), MAX_REFUND_AMOUNT), validationData);
            return createResponse(request, false, reasoning, createMetadata("AMOUNT_EXCEEDS_LIMIT"));
        }
        
        // Check risk assessment
        RiskAssessmentTool.RiskAssessmentResult riskAssessment = 
            (RiskAssessmentTool.RiskAssessmentResult) fullContext.get("riskAssessment");
        
        if (riskAssessment != null && "HIGH".equals(riskAssessment.getRiskLevel())) {
            String reasoning = generateReasoning(request, 
                "High risk detected: " + riskAssessment.getRiskAnalysis(), validationData);
            return createResponse(request, false, reasoning, createMetadata("HIGH_RISK"));
        }
        
        // Generate AI reasoning with validation data and risk assessment
        String additionalContext = String.format(
            "Refund amount: %s %s, Reason: %s, Customer: %s, Risk Level: %s, Risk Score: %d",
            request.getCurrency(), request.getRefundAmount(), 
            request.getReason(), request.getCustomerId(),
            riskAssessment != null ? riskAssessment.getRiskLevel() : "UNKNOWN",
            riskAssessment != null ? riskAssessment.getRiskScore() : 0
        );
        
        String reasoning = generateReasoning(request, additionalContext, validationData);
        
        // Determine approval based on AI reasoning and risk assessment
        boolean approved = determineApproval(reasoning, riskAssessment);
        
        Map<String, Object> metadata = createMetadata("REVIEW_COMPLETED");
        metadata.put("reviewedAmount", request.getRefundAmount());
        metadata.put("reviewedReason", request.getReason());
        if (riskAssessment != null) {
            metadata.put("riskScore", riskAssessment.getRiskScore());
            metadata.put("riskLevel", riskAssessment.getRiskLevel());
        }
        
        return createResponse(request, approved, reasoning, metadata);
    }
    
    @Override
    protected String buildPrompt(RefundRequest request, String additionalContext) {
        return String.format("""
            Please review this credit balance refund request:
            
            Request ID: %s
            Customer ID: %s
            Account Number: %s
            Refund Amount: %s %s
            Reason: %s
            Request Date: %s
            
            Additional Context: %s
            
            Please provide your decision (APPROVE or REJECT) with detailed reasoning.
            Consider:
            1. Is the amount reasonable?
            2. Is the reason valid?
            3. Is all information complete?
            4. Does this comply with our policies?
            5. What are the risk markers?
            6. What is the repayment history?
            """,
            request.getRequestId(),
            request.getCustomerId(),
            request.getAccountNumber(),
            request.getCurrency(),
            request.getRefundAmount(),
            request.getReason(),
            request.getRequestTimestamp(),
            additionalContext
        );
    }
    
    private boolean validateRequest(RefundRequest request) {
        return request.getCustomerId() != null && !request.getCustomerId().isEmpty()
            && request.getAccountNumber() != null && !request.getAccountNumber().isEmpty()
            && request.getRefundAmount() != null && request.getRefundAmount().compareTo(BigDecimal.ZERO) > 0
            && request.getReason() != null && !request.getReason().isEmpty()
            && request.getCurrency() != null && !request.getCurrency().isEmpty();
    }
    
    private boolean determineApproval(String reasoning, RiskAssessmentTool.RiskAssessmentResult riskAssessment) {
        String upperReasoning = reasoning.toUpperCase();
        
        // Check LLM decision
        boolean llmApproves = upperReasoning.contains("DECISION: APPROVE") 
            || (upperReasoning.contains("APPROVE") 
                && !upperReasoning.contains("REJECT")
                && !upperReasoning.contains("MISSING")
                && !upperReasoning.contains("INVALID"));
        
        // Consider risk assessment
        if (riskAssessment != null) {
            // High risk should generally lead to rejection
            if ("HIGH".equals(riskAssessment.getRiskLevel())) {
                return false;
            }
            // Moderate risk requires strong LLM approval
            if ("MODERATE".equals(riskAssessment.getRiskLevel()) && !llmApproves) {
                return false;
            }
        }
        
        return llmApproves;
    }
    
    private Map<String, Object> createMetadata(String status) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("status", status);
        metadata.put("agent", AGENT_NAME);
        return metadata;
    }
}
