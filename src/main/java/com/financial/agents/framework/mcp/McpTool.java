package com.financial.agents.framework.mcp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MCP Tool definition
 * Represents a tool that can be called via Model Context Protocol
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpTool {
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
    
    public static McpToolBuilder builder() {
        return new McpToolBuilder();
    }
    
    public static class McpToolBuilder {
        private String name;
        private String description;
        private Map<String, Object> inputSchema;
        
        public McpToolBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public McpToolBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public McpToolBuilder inputSchema(Map<String, Object> inputSchema) {
            this.inputSchema = inputSchema;
            return this;
        }
        
        public McpTool build() {
            return new McpTool(name, description, inputSchema);
        }
    }
}
