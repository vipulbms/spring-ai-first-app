package com.financial.agents.framework.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    private String agentName;
    private String requestId;
    private boolean approved;
    private String reasoning;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private AgentType agentType;
}
