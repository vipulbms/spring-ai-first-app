package com.financial.agents.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI 2.0.0-M1
 * Spring AI auto-configuration will handle ChatModel and ChatClient beans
 * Configuration is done via application.yml
 * 
 * MCP tools are registered via McpToolRegistry and can be used with:
 * - Spring AI ChatClient (when MCP support is available)
 * - LangChain4j agents (via McpToolAdapter)
 * - Direct tool execution (via McpToolRegistry)
 */
@Slf4j
@Configuration
public class SpringAiConfig {
    
    // Spring AI 2.0.0-M1 auto-configuration handles:
    // - ChatModel beans from spring-ai-ollama-spring-boot-starter
    // - ChatClient beans
    // - MCP server/client configuration via application.yml
    
    // Custom configuration can be added here if needed
}
