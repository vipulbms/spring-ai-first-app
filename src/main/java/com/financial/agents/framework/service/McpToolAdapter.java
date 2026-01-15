package com.financial.agents.framework.service;

import com.financial.agents.framework.mcp.McpTool;
import com.financial.agents.framework.mcp.McpToolRegistry;
import dev.langchain4j.agent.tool.ToolSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter to convert MCP tools to LangChain4j tool specifications
 * Enables using MCP tools with LangChain4j agents
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolAdapter {
    
    private final McpToolRegistry toolRegistry;
    
    /**
     * Get LangChain4j tool specifications from MCP tools
     */
    public List<ToolSpecification> getLangChain4jTools() {
        List<McpTool> mcpTools = toolRegistry.getValidationTools();
        
        return mcpTools.stream()
            .map(this::convertToLangChain4j)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert MCP Tool to LangChain4j ToolSpecification
     */
    private ToolSpecification convertToLangChain4j(McpTool mcpTool) {
        // Extract schema information
        Map<String, Object> inputSchema = mcpTool.getInputSchema();
        
        // Build LangChain4j tool specification
        // Note: This is a simplified conversion
        // In production, you'd need to properly map the JSON schema
        return ToolSpecification.builder()
            .name(mcpTool.getName())
            .description(mcpTool.getDescription())
            // Schema conversion would go here
            .build();
    }
    
    /**
     * Execute MCP tool and return result as string for LangChain4j
     */
    public String executeMcpTool(String toolName, Map<String, Object> arguments) {
        Object result = toolRegistry.executeTool(toolName, arguments);
        
        // Convert result to string format for LLM
        if (result instanceof String) {
            return (String) result;
        } else {
            // Convert object to JSON string
            return result.toString();
        }
    }
    
    /**
     * Get all available MCP tool names
     */
    public List<String> getAvailableToolNames() {
        return toolRegistry.getValidationTools().stream()
            .map(McpTool::getName)
            .collect(Collectors.toList());
    }
}
