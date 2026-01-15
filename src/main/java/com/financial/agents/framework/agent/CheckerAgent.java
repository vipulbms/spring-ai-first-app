package com.financial.agents.framework.agent;

import com.financial.agents.framework.domain.AgentResponse;
import com.financial.agents.framework.domain.AgentType;
import com.financial.agents.framework.domain.RefundRequest;
import com.financial.agents.framework.memory.MemoryService;
import com.financial.agents.framework.service.AgentToolService;
import com.financial.agents.framework.service.AuditService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CheckerAgent extends BaseAgent {
    
    private static final String AGENT_NAME = "CheckerAgent";
    
    public CheckerAgent(ChatLanguageModel chatModel, AuditService auditService, 
                       MemoryService memoryService, AgentToolService agentToolService) {
        super(chatModel, auditService, memoryService, agentToolService, AGENT_NAME, AgentType.CHECKER);
    }
    
    @Override
    protected boolean enableOptionalTools() {
        return true; // Enable optional tool calling for CheckerAgent
    }
    
    @Override
    protected String getSystemPrompt() {
        return """
            You are a Checker Agent in a financial institution responsible for reviewing and validating the decisions made by the Maker Agent.
            Your role is to:
            1. Review the Maker Agent's decision and reasoning
            2. Verify the Maker Agent followed proper procedures
            3. Check for any inconsistencies or errors
            4. Validate that all compliance requirements are met
            5. Make a final decision: APPROVE or REJECT the Maker's decision
            
            Guidelines:
            - You must be thorough and independent in your review
            - If the Maker's reasoning is sound and complete, you should APPROVE
            - If you find any issues, inconsistencies, or missing validations, you should REJECT
            - Your decision overrides the Maker's decision
            - Provide clear reasoning for your decision
            
            Format your response as: DECISION: [APPROVE/REJECT] REASONING: [Your detailed reasoning]
            This is a critical control point in the financial process. Be meticulous.
            """;
    }
    
    @Override
    protected AgentResponse processRequest(RefundRequest request, Map<String, Object> context) {
        log.info("Checker agent reviewing maker decision for request: {}", request.getRequestId());
        
        // Get maker's response from context
        AgentResponse makerResponse = (AgentResponse) context.get("makerResponse");
        if (makerResponse == null) {
            String reasoning = "Maker response not found in context. Cannot proceed with check.";
            return createResponse(request, false, reasoning, createMetadata("MAKER_RESPONSE_MISSING"));
        }
        
        // Generate AI reasoning for checker review
        String additionalContext = String.format(
            "Maker Decision: %s, Maker Reasoning: %s, Request: %s",
            makerResponse.isApproved() ? "APPROVED" : "REJECTED",
            makerResponse.getReasoning(),
            request.getRequestId()
        );
        
        String reasoning = generateReasoning(request, additionalContext);
        
        // Determine if checker approves maker's decision
        boolean approved = determineCheckerApproval(reasoning, makerResponse);
        
        Map<String, Object> metadata = createMetadata("CHECK_COMPLETED");
        metadata.put("makerApproved", makerResponse.isApproved());
        metadata.put("makerReasoning", makerResponse.getReasoning());
        metadata.put("checkerDecision", approved ? "APPROVED" : "REJECTED");
        
        return createResponse(request, approved, reasoning, metadata);
    }
    
    @Override
    protected String buildPrompt(RefundRequest request, String additionalContext) {
        return String.format("""
            Please review the Maker Agent's decision for this credit balance refund request:
            
            Request ID: %s
            Customer ID: %s
            Account Number: %s
            Refund Amount: %s %s
            Reason: %s
            
            Additional Context: %s
            
            Please review the Maker Agent's decision and reasoning. Determine if:
            1. The Maker's decision is appropriate
            2. The Maker's reasoning is complete and logical
            3. All required validations were performed
            4. There are any inconsistencies or errors
            5. Compliance requirements are met
            """,
            request.getRequestId(),
            request.getCustomerId(),
            request.getAccountNumber(),
            request.getCurrency(),
            request.getRefundAmount(),
            request.getReason(),
            additionalContext
        );
    }
    
    private boolean determineCheckerApproval(String reasoning, AgentResponse makerResponse) {
        String upperReasoning = reasoning.toUpperCase();
        
        // If maker rejected, checker can still approve (override)
        // If maker approved, checker validates
        if (makerResponse.isApproved()) {
            // Checker validates maker's approval
            return upperReasoning.contains("DECISION: APPROVE") 
                || (upperReasoning.contains("APPROVE") 
                    && !upperReasoning.contains("REJECT")
                    && !upperReasoning.contains("ERROR")
                    && !upperReasoning.contains("INCONSISTENCY"));
        } else {
            // Checker can override maker's rejection if appropriate
            return upperReasoning.contains("DECISION: APPROVE") 
                || (upperReasoning.contains("APPROVE") 
                    && (upperReasoning.contains("OVERRIDE") || upperReasoning.contains("CORRECT")));
        }
    }
    
    private Map<String, Object> createMetadata(String status) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("status", status);
        metadata.put("agent", AGENT_NAME);
        return metadata;
    }
}
