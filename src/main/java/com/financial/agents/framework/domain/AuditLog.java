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
public class AuditLog {
    private String logId;
    private String requestId;
    private AgentType agentType;
    private String agentName;
    private String action;
    private String status;
    private String details;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private String userId;
}
