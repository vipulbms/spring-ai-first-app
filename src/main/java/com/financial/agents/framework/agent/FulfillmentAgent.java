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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class FulfillmentAgent extends BaseAgent {
    
    private static final String AGENT_NAME = "FulfillmentAgent";
    
    public FulfillmentAgent(ChatLanguageModel chatModel, AuditService auditService, 
                           MemoryService memoryService, AgentToolService agentToolService) {
        super(chatModel, auditService, memoryService, agentToolService, AGENT_NAME, AgentType.FULFILLMENT);
    }
    
    @Override
    protected boolean enableOptionalTools() {
        return false; // Fulfillment agent doesn't need optional tools
    }
    
    @Override
    protected String getSystemPrompt() {
        return """
            You are a Fulfillment Agent in a financial institution responsible for executing approved refund transactions.
            Your role is to:
            1. Process the refund transaction if approved by both Maker and Checker
            2. Generate transaction confirmation details
            3. Prepare customer notification message
            4. Ensure transaction is properly recorded
            5. Handle any transaction errors gracefully
            
            Guidelines:
            - Only process transactions that have been approved by both Maker and Checker
            - Generate clear, professional confirmation messages
            - Include transaction ID, amount, and expected processing time
            - If transaction fails, provide clear error message
            
            Format your response as: TRANSACTION_STATUS: [SUCCESS/FAILED] CONFIRMATION_MESSAGE: [Professional message for customer]
            Be professional and customer-focused in all communications.
            """;
    }
    
    @Override
    protected AgentResponse processRequest(RefundRequest request, Map<String, Object> context) {
        log.info("Fulfillment agent processing request: {}", request.getRequestId());
        
        // Get checker's response from context
        AgentResponse checkerResponse = (AgentResponse) context.get("checkerResponse");
        if (checkerResponse == null || !checkerResponse.isApproved()) {
            String reasoning = "Request not approved by Checker. Cannot fulfill transaction.";
            return createResponse(request, false, reasoning, createMetadata("NOT_APPROVED"));
        }
        
        // Process the transaction
        boolean transactionSuccess = processTransaction(request);
        
        if (!transactionSuccess) {
            String reasoning = generateReasoning(request, "Transaction processing failed. Please retry or contact support.");
            return createResponse(request, false, reasoning, createMetadata("TRANSACTION_FAILED"));
        }
        
        // Generate confirmation message
        String confirmationMessage = generateConfirmationMessage(request);
        
        // Send confirmation to customer
        boolean notificationSent = sendConfirmation(request, confirmationMessage);
        
        String reasoning = generateReasoning(request, 
            String.format("Transaction processed successfully. Confirmation sent: %s", notificationSent));
        
        Map<String, Object> metadata = createMetadata("FULFILLMENT_COMPLETED");
        metadata.put("transactionId", generateTransactionId(request));
        metadata.put("transactionTimestamp", LocalDateTime.now());
        metadata.put("confirmationSent", notificationSent);
        metadata.put("confirmationMessage", confirmationMessage);
        
        return createResponse(request, true, reasoning, metadata);
    }
    
    @Override
    protected String buildPrompt(RefundRequest request, String additionalContext) {
        return String.format("""
            Please process this approved refund transaction:
            
            Request ID: %s
            Customer ID: %s
            Account Number: %s
            Refund Amount: %s %s
            Customer Email: %s
            Customer Phone: %s
            
            Additional Context: %s
            
            Please:
            1. Confirm the transaction will be processed
            2. Generate a professional confirmation message for the customer
            3. Include transaction details and expected processing time
            """,
            request.getRequestId(),
            request.getCustomerId(),
            request.getAccountNumber(),
            request.getCurrency(),
            request.getRefundAmount(),
            request.getCustomerEmail(),
            request.getCustomerPhone(),
            additionalContext
        );
    }
    
    private boolean processTransaction(RefundRequest request) {
        // Simulate transaction processing
        // In production, this would integrate with payment processing system
        log.info("Processing refund transaction for request: {}", request.getRequestId());
        try {
            // Simulate processing delay
            Thread.sleep(100);
            // In production: call payment gateway, update account balance, etc.
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    private String generateConfirmationMessage(RefundRequest request) {
        String transactionId = generateTransactionId(request);
        return String.format(
            "Dear Customer,\n\n" +
            "Your refund request has been processed successfully.\n\n" +
            "Transaction ID: %s\n" +
            "Refund Amount: %s %s\n" +
            "Account: %s\n" +
            "Expected processing time: 3-5 business days\n\n" +
            "Thank you for your patience.\n\n" +
            "Best regards,\nFinancial Institution",
            transactionId,
            request.getCurrency(),
            request.getRefundAmount(),
            request.getAccountNumber()
        );
    }
    
    private boolean sendConfirmation(RefundRequest request, String message) {
        // Simulate sending confirmation
        // In production, this would send email/SMS
        log.info("Sending confirmation to customer {}: {}", request.getCustomerId(), message);
        // In production: sendEmail(request.getCustomerEmail(), message);
        // In production: sendSMS(request.getCustomerPhone(), message);
        return true;
    }
    
    private String generateTransactionId(RefundRequest request) {
        return "TXN-" + request.getRequestId() + "-" + System.currentTimeMillis();
    }
    
    private Map<String, Object> createMetadata(String status) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("status", status);
        metadata.put("agent", AGENT_NAME);
        return metadata;
    }
}
