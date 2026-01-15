package com.financial.agents.framework.service;

import com.financial.agents.framework.domain.AgentType;
import com.financial.agents.framework.domain.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AuditService {
    
    private final AuditRepository auditRepository;
    
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }
    
    @Async
    public CompletableFuture<Void> logAction(AgentType agentType, String agentName, 
                                            String action, String details, 
                                            String requestId, Object context) {
        return CompletableFuture.runAsync(() -> {
            try {
                AuditLog auditLog = AuditLog.builder()
                    .logId(UUID.randomUUID().toString())
                    .requestId(requestId)
                    .agentType(agentType)
                    .agentName(agentName)
                    .action(action)
                    .status("SUCCESS")
                    .details(details)
                    .metadata(createMetadata(context))
                    .timestamp(LocalDateTime.now())
                    .userId("SYSTEM")
                    .build();
                
                // Log to console (in production, persist to database)
                logAudit(auditLog);
                
                // Persist to repository (async)
                auditRepository.save(auditLog);
                
            } catch (Exception e) {
                log.error("Error in async audit logging", e);
                // Log error synchronously to ensure it's captured
                log.error("Failed to audit: Agent={}, Action={}, RequestId={}", 
                    agentName, action, requestId);
            }
        });
    }
    
    private void logAudit(AuditLog auditLog) {
        log.info("AUDIT: [{}] {} - {} - {} - Request: {} - Details: {}", 
            auditLog.getTimestamp(),
            auditLog.getAgentType(),
            auditLog.getAgentName(),
            auditLog.getAction(),
            auditLog.getRequestId(),
            auditLog.getDetails());
    }
    
    private java.util.Map<String, Object> createMetadata(Object context) {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        if (context != null) {
            metadata.put("contextType", context.getClass().getSimpleName());
            metadata.put("contextHash", String.valueOf(context.hashCode()));
        }
        return metadata;
    }
}
