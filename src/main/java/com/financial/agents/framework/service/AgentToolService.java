package com.financial.agents.framework.service;

import com.financial.agents.framework.tool.OptionalValidationTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for creating AI services with tool calling support
 * Supports both LangChain4j tools and MCP tools
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentToolService {
    
    private final ChatLanguageModel chatModel;
    private final OptionalValidationTools optionalTools;
    private final McpToolAdapter mcpToolAdapter;
    
    /**
     * Create an AI service interface with tool calling support
     * The LLM can call tools from OptionalValidationTools and MCP tools
     */
    public AgentWithToolsInterface createAgentWithTools(ChatMemory memory) {
        return AiServices.builder(AgentWithToolsInterface.class)
            .chatLanguageModel(chatModel)
            .chatMemory(memory)
            .tools(optionalTools) // LangChain4j tools
            // MCP tools can be added here when LangChain4j supports them directly
            // For now, MCP tools are available via Spring AI MCP server
            .build();
    }
    
    /**
     * Get all available tool names (both LangChain4j and MCP)
     */
    public java.util.List<String> getAllToolNames() {
        java.util.List<String> toolNames = new java.util.ArrayList<>();
        
        // Add MCP tool names
        toolNames.addAll(mcpToolAdapter.getAvailableToolNames());
        
        // LangChain4j tool names are automatically discovered from @Tool annotations
        // They're available through the optionalTools instance
        
        return toolNames;
    }
    
    /**
     * Interface for agent reasoning with tool calling
     */
    public interface AgentWithToolsInterface {
        String generateReasoning(String prompt);
    }
}
