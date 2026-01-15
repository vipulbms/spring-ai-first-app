package com.financial.agents.framework.config;

import com.financial.agents.framework.mcp.McpToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Model Context Protocol (MCP) integration
 * MCP tools are registered and available via McpToolRegistry
 */
@Slf4j
@Configuration
public class McpConfig {
    
    private final McpToolRegistry toolRegistry;
    
    public McpConfig(McpToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
        log.info("MCP Configuration initialized with {} tools", 
            toolRegistry.getValidationTools().size());
    }
    
    /**
     * MCP tools are available through McpToolRegistry
     * Spring AI 2.0.0-M1 MCP integration will be configured via application.yml
     * Tools can be accessed via REST endpoints or through LangChain4j adapters
     */
}
