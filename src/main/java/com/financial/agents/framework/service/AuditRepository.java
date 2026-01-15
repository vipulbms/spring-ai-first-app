package com.financial.agents.framework.service;

import com.financial.agents.framework.domain.AuditLog;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Repository
public class AuditRepository {
    
    // In-memory storage for demo purposes
    // In production, replace with database repository (JPA, MongoDB, etc.)
    private final ConcurrentMap<String, AuditLog> auditLogs = new ConcurrentHashMap<>();
    
    public void save(AuditLog auditLog) {
        auditLogs.put(auditLog.getLogId(), auditLog);
    }
    
    public List<AuditLog> findByRequestId(String requestId) {
        return auditLogs.values().stream()
            .filter(log -> requestId.equals(log.getRequestId()))
            .collect(Collectors.toList());
    }
    
    public List<AuditLog> findAll() {
        return new ArrayList<>(auditLogs.values());
    }
    
    public void clear() {
        auditLogs.clear();
    }
}
