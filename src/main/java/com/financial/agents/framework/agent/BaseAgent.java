package com.financial.agents.framework.agent;

import com.financial.agents.framework.domain.AgentResponse;
import com.financial.agents.framework.domain.AgentType;
import com.financial.agents.framework.domain.RefundRequest;
import com.financial.agents.framework.memory.MemoryService;
import com.financial.agents.framework.service.AgentToolService;
import com.financial.agents.framework.service.AuditService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseAgent {
    
    protected final ChatLanguageModel chatModel;
    protected final AuditService auditService;
    protected final MemoryService memoryService;
    protected final AgentToolService agentToolService;
    protected final String agentName;
    protected final AgentType agentType;
    
    /**
     * Whether to enable optional tool calling for this agent
     * Override in subclasses to enable/disable
     */
    protected boolean enableOptionalTools() {
        return false; // Default: disabled
    }
    
    protected abstract String getSystemPrompt();
    protected abstract AgentResponse processRequest(RefundRequest request, Map<String, Object> context);
    
    public AgentResponse execute(RefundRequest request, Map<String, Object> context) {
        log.info("{} agent executing for request: {}", agentName, request.getRequestId());
        
        try {
            auditService.logAction(agentType, agentName, "EXECUTION_STARTED", 
                "Processing request: " + request.getRequestId(), request.getRequestId(), context);
            
            AgentResponse response = processRequest(request, context);
            
            auditService.logAction(agentType, agentName, "EXECUTION_COMPLETED", 
                "Response: " + (response.isApproved() ? "APPROVED" : "REJECTED"), 
                request.getRequestId(), context);
            
            return response;
        } catch (Exception e) {
            log.error("Error in {} agent execution", agentName, e);
            auditService.logAction(agentType, agentName, "EXECUTION_FAILED", 
                "Error: " + e.getMessage(), request.getRequestId(), context);
            throw new AgentExecutionException("Agent execution failed: " + e.getMessage(), e);
        }
    }
    
    protected String generateReasoning(RefundRequest request, String additionalContext) {
        return generateReasoning(request, additionalContext, null);
    }
    
    protected String generateReasoning(RefundRequest request, String additionalContext, Map<String, Object> validationData) {
        String prompt = buildPrompt(request, additionalContext, validationData);
        String fullPrompt = getSystemPrompt() + "\n\n" + prompt;
        
        // Get or create memory for this request
        ChatMemory memory = memoryService.getOrCreateMemory(request.getRequestId());
        
        // Check if optional tools are enabled
        if (enableOptionalTools() && agentToolService != null) {
            return generateReasoningWithTools(fullPrompt, memory);
        } else {
            return generateReasoningWithoutTools(fullPrompt, memory);
        }
    }
    
    /**
     * Generate reasoning with optional tool calling support
     * LLM can call tools if it needs additional information
     */
    private String generateReasoningWithTools(String fullPrompt, ChatMemory memory) {
        log.debug("Generating reasoning with optional tool calling enabled");
        
        try {
            // Create AI service with tool calling support
            AgentToolService.AgentWithToolsInterface agentWithTools = 
                agentToolService.createAgentWithTools(memory);
            
            // Add user message to memory
            memory.add(dev.langchain4j.data.message.UserMessage.userMessage(fullPrompt));
            
            // Generate response - LLM can call tools if needed
            String reasoning = agentWithTools.generateReasoning(fullPrompt);
            
            log.debug("Generated reasoning with tools: {}", reasoning);
            return reasoning;
        } catch (Exception e) {
            log.warn("Error with tool calling, falling back to standard reasoning", e);
            return generateReasoningWithoutTools(fullPrompt, memory);
        }
    }
    
    /**
     * Generate reasoning without tool calling (standard approach)
     */
    private String generateReasoningWithoutTools(String fullPrompt, ChatMemory memory) {
        // Add user message to memory
        memory.add(dev.langchain4j.data.message.UserMessage.userMessage(fullPrompt));
        
        // Generate response using LLM with memory
        dev.langchain4j.data.message.AiMessage aiMessage = chatModel.generate(memory.messages()).content();
        String reasoning = aiMessage.text();
        
        // Add AI response to memory
        memory.add(aiMessage);
        
        log.debug("Generated reasoning: {}", reasoning);
        return reasoning;
    }
    
    protected abstract String buildPrompt(RefundRequest request, String additionalContext);
    
    protected String buildPrompt(RefundRequest request, String additionalContext, Map<String, Object> validationData) {
        // Default implementation - can be overridden
        String basePrompt = buildPrompt(request, additionalContext);
        
        if (validationData != null && !validationData.isEmpty()) {
            StringBuilder enhancedPrompt = new StringBuilder(basePrompt);
            enhancedPrompt.append("\n\nValidation Data:\n");
            
            // Add validation data to prompt
            if (validationData.containsKey("customerValidation")) {
                enhancedPrompt.append("Customer Validation: ").append(validationData.get("customerValidation")).append("\n");
            }
            if (validationData.containsKey("accountValidation")) {
                enhancedPrompt.append("Account Validation: ").append(validationData.get("accountValidation")).append("\n");
            }
            if (validationData.containsKey("cardValidation")) {
                enhancedPrompt.append("Card Validation: ").append(validationData.get("cardValidation")).append("\n");
            }
            if (validationData.containsKey("riskAssessment")) {
                enhancedPrompt.append("Risk Assessment: ").append(validationData.get("riskAssessment")).append("\n");
            }
            
            return enhancedPrompt.toString();
        }
        
        return basePrompt;
    }
    
    protected AgentResponse createResponse(RefundRequest request, boolean approved, 
                                          String reasoning, Map<String, Object> metadata) {
        return AgentResponse.builder()
            .agentName(agentName)
            .requestId(request.getRequestId())
            .approved(approved)
            .reasoning(reasoning)
            .metadata(metadata != null ? metadata : new HashMap<>())
            .timestamp(LocalDateTime.now())
            .agentType(agentType)
            .build();
    }
    
    public static class AgentExecutionException extends RuntimeException {
        public AgentExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
