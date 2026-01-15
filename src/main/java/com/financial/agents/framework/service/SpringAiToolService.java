package com.financial.agents.framework.service;

import com.financial.agents.framework.mcp.McpTool;
import com.financial.agents.framework.mcp.McpToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for using Spring AI with MCP tools
 * Provides integration between Spring AI and MCP protocol
 * 
 * Note: Spring AI 2.0.0-M1 MCP integration will be available through
 * auto-configuration. This service provides helper methods for MCP tool access.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpringAiToolService {
    
    private final McpToolRegistry mcpToolRegistry;
    
    /**
     * Execute MCP tool and return result
     * Can be used with Spring AI ChatClient when MCP tools are registered
     */
    public Object executeMcpTool(String toolName, Map<String, Object> arguments) {
        log.debug("Executing MCP tool via SpringAiToolService: {}", toolName);
        return mcpToolRegistry.executeTool(toolName, arguments);
    }
    
    /**
     * Get all available MCP tools
     */
    public List<McpTool> getAvailableTools() {
        return mcpToolRegistry.getValidationTools();
    }
    
    /**
     * Get MCP tool by name
     */
    public McpTool getTool(String toolName) {
        return mcpToolRegistry.getValidationTools().stream()
            .filter(tool -> tool.getName().equals(toolName))
            .findFirst()
            .orElse(null);
    }
}
