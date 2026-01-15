package com.financial.agents.framework.service;

import com.financial.agents.framework.domain.AgentResponse;
import com.financial.agents.framework.domain.RefundRequest;
import com.financial.agents.framework.domain.RefundState;
import com.financial.agents.framework.domain.RefundStatus;
import com.financial.agents.framework.graph.RefundStateGraph;
import com.financial.agents.framework.memory.MemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundOrchestrationService {
    
    private final RefundStateGraph stateGraph;
    private final MemoryService memoryService;
    
    public RefundProcessResult processRefund(RefundRequest request) {
        log.info("Starting refund orchestration with LangGraph for request: {}", request.getRequestId());
        
        // Initialize state
        RefundState initialState = RefundState.initial(request);
        
        // Execute the state graph
        RefundState finalState = stateGraph.execute(initialState);
        
        // Build result
        RefundProcessResult result = new RefundProcessResult();
        result.setRequestId(request.getRequestId());
        result.setStatus(finalState.getCurrentStatus());
        result.setMakerResponse(finalState.getMakerResponse());
        result.setCheckerResponse(finalState.getCheckerResponse());
        result.setFulfillmentResponse(finalState.getFulfillmentResponse());
        
        // Set final message
        if (finalState.getCurrentStatus() == RefundStatus.FULFILLED) {
            result.setFinalMessage("Refund processed successfully. " + 
                (finalState.getFulfillmentResponse() != null ? 
                    finalState.getFulfillmentResponse().getReasoning() : ""));
        } else if (finalState.getCurrentStatus() == RefundStatus.REJECTED) {
            result.setFinalMessage(finalState.getErrorMessage() != null ? 
                finalState.getErrorMessage() : "Request rejected");
        } else if (finalState.getCurrentStatus() == RefundStatus.FAILED) {
            result.setFinalMessage("Fulfillment failed: " + 
                (finalState.getFulfillmentResponse() != null ? 
                    finalState.getFulfillmentResponse().getReasoning() : "Unknown error"));
        } else {
            result.setFinalMessage("Process completed with status: " + finalState.getCurrentStatus());
        }
        
        // Clean up memory after processing (optional - can keep for audit)
        // memoryService.clearMemory(request.getRequestId());
        
        return result;
    }
    
    public static class RefundProcessResult {
        private String requestId;
        private RefundStatus status;
        private String finalMessage;
        private AgentResponse makerResponse;
        private AgentResponse checkerResponse;
        private AgentResponse fulfillmentResponse;
        
        // Getters and setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public RefundStatus getStatus() { return status; }
        public void setStatus(RefundStatus status) { this.status = status; }
        
        public String getFinalMessage() { return finalMessage; }
        public void setFinalMessage(String finalMessage) { this.finalMessage = finalMessage; }
        
        public AgentResponse getMakerResponse() { return makerResponse; }
        public void setMakerResponse(AgentResponse makerResponse) { this.makerResponse = makerResponse; }
        
        public AgentResponse getCheckerResponse() { return checkerResponse; }
        public void setCheckerResponse(AgentResponse checkerResponse) { this.checkerResponse = checkerResponse; }
        
        public AgentResponse getFulfillmentResponse() { return fulfillmentResponse; }
        public void setFulfillmentResponse(AgentResponse fulfillmentResponse) { this.fulfillmentResponse = fulfillmentResponse; }
    }
}
