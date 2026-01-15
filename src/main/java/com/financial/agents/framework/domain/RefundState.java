package com.financial.agents.framework.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundState {
    private RefundRequest request;
    private AgentResponse makerResponse;
    private AgentResponse checkerResponse;
    private AgentResponse fulfillmentResponse;
    private RefundStatus currentStatus;
    private String errorMessage;
    private Map<String, Object> context;
    
    public static RefundState initial(RefundRequest request) {
        return RefundState.builder()
            .request(request)
            .currentStatus(RefundStatus.PENDING_REVIEW)
            .context(new HashMap<>())
            .build();
    }
    
    public void addToContext(String key, Object value) {
        if (context == null) {
            context = new HashMap<>();
        }
        context.put(key, value);
    }
}
