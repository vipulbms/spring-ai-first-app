# Spring AI 2.0.0-M1 and MCP Integration

This document explains the integration of Spring AI 2.0.0-M1, MCP (Model Context Protocol), LangChain4j, and LangGraph in the credit refund agent framework.

## Overview

The framework now supports multiple AI integration approaches:

1. **Spring AI 2.0.0-M1**: Latest Spring AI with auto-configuration
2. **MCP (Model Context Protocol)**: Standardized tool protocol for LLMs
3. **LangChain4j**: Java port of LangChain for LLM interactions
4. **LangGraph**: Custom state machine for agent orchestration

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Agent Framework                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────┐    ┌──────────────┐                  │
│  │  LangChain4j │    │  Spring AI   │                  │
│  │   (Primary)  │    │  2.0.0-M1    │                  │
│  └──────┬───────┘    └──────┬───────┘                  │
│         │                   │                          │
│         └─────────┬─────────┘                          │
│                   │                                     │
│            ┌──────▼──────┐                              │
│            │  MCP Tools  │                              │
│            │   Registry  │                              │
│            └──────┬──────┘                              │
│                   │                                     │
│         ┌─────────┴─────────┐                          │
│         │                   │                           │
│  ┌──────▼──────┐   ┌───────▼──────┐                   │
│  │ Validation  │   │   Optional   │                   │
│  │   Tools     │   │     Tools     │                   │
│  └─────────────┘   └───────────────┘                   │
│                                                         │
│  ┌──────────────────────────────────────┐              │
│  │      LangGraph State Machine         │              │
│  │   (Maker → Checker → Fulfillment)    │              │
│  └──────────────────────────────────────┘              │
└─────────────────────────────────────────────────────────┘
```

## Spring AI 2.0.0-M1

### Configuration

Spring AI 2.0.0-M1 is configured via `application.yml`:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.2
          temperature: 0.7
```

### Features

- **Auto-configuration**: ChatModel and ChatClient beans are auto-configured
- **Ollama Integration**: Native support for Ollama models
- **MCP Support**: MCP server/client support (when available)
- **Tool Calling**: Enhanced tool calling capabilities

### Usage

Spring AI ChatClient can be injected and used:

```java
@Autowired
private ChatClient chatClient;

public String generateResponse(String prompt) {
    return chatClient.call(prompt).getResult().getOutput().getContent();
}
```

## MCP (Model Context Protocol)

### What is MCP?

MCP is a standardized protocol for exposing tools and context to language models. It provides:
- **Standardized Tool Interface**: Consistent way to define and expose tools
- **Tool Discovery**: LLMs can discover available tools
- **Tool Execution**: Standardized way to execute tools
- **Interoperability**: Works across different LLM providers

### MCP Tool Registry

The `McpToolRegistry` manages all MCP-compliant tools:

```java
@Component
public class McpToolRegistry {
    public List<McpTool> getValidationTools() {
        // Returns all registered MCP tools
    }
    
    public Object executeTool(String toolName, Map<String, Object> arguments) {
        // Executes MCP tool by name
    }
}
```

### Available MCP Tools

1. **validate_customer**: Validates customer information
2. **validate_account**: Validates account details and repayment history
3. **validate_card**: Validates card details
4. **get_customer_transaction_history**: Gets transaction history
5. **check_similar_refunds**: Checks for similar refund patterns
6. **get_account_activity_summary**: Gets account activity summary

### MCP Tool Definition

Each tool is defined with:
- **Name**: Unique tool identifier
- **Description**: What the tool does
- **Input Schema**: JSON schema defining parameters

Example:
```java
McpTool.builder()
    .name("validate_customer")
    .description("Validates customer information and checks for risk markers")
    .inputSchema(schema)
    .build();
```

## Integration Points

### 1. LangChain4j + MCP

MCP tools are converted to LangChain4j tool specifications:

```java
@Autowired
private McpToolAdapter mcpToolAdapter;

List<ToolSpecification> tools = mcpToolAdapter.getLangChain4jTools();
// Use with LangChain4j agents
```

### 2. Spring AI + MCP

MCP tools can be used with Spring AI ChatClient:

```java
@Autowired
private SpringAiToolService springAiToolService;

// MCP tools are available via registry
List<McpTool> tools = springAiToolService.getAvailableTools();
```

### 3. LangGraph + MCP

LangGraph state machine can use MCP tools through agents:

```
State Graph → Agent → MCP Tool → Result → Next State
```

## Tool Execution Flow

### With LangChain4j

```
1. LLM decides to call tool
   ↓
2. LangChain4j agent receives tool call
   ↓
3. McpToolAdapter converts to MCP tool
   ↓
4. McpToolRegistry.executeTool()
   ↓
5. Validation tool executes
   ↓
6. Result returned to LLM
```

### With Spring AI

```
1. Spring AI ChatClient with MCP tools
   ↓
2. LLM calls MCP tool
   ↓
3. SpringAiToolService.executeMcpTool()
   ↓
4. McpToolRegistry.executeTool()
   ↓
5. Result returned to ChatClient
```

## Configuration

### application.yml

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.2
          temperature: 0.7

# MCP Configuration
mcp:
  server:
    name: refund-validation-tools
    version: 1.0.0
  client:
    enabled: false
```

## Adding New MCP Tools

### Step 1: Create Tool Implementation

```java
@Component
public class MyValidationTool {
    public MyResult validateSomething(String param) {
        // Implementation
    }
}
```

### Step 2: Register in McpToolRegistry

```java
tools.add(McpTool.builder()
    .name("validate_something")
    .description("Validates something")
    .inputSchema(schema)
    .build());
```

### Step 3: Add Execution Logic

```java
case "validate_something":
    String param = (String) arguments.get("param");
    return myTool.validateSomething(param);
```

## Benefits

### 1. **Standardization**
- MCP provides a standard way to expose tools
- Works with any MCP-compliant LLM client
- Interoperable across different systems

### 2. **Flexibility**
- Tools can be used with Spring AI or LangChain4j
- Easy to add new tools
- Tools are discoverable by LLMs

### 3. **Extensibility**
- Add new tools without changing agent code
- Tools can be shared across agents
- Easy to version and document tools

### 4. **Integration**
- Works with existing validation tools
- Compatible with optional tool calling
- Integrates with LangGraph state machine

## Current Status

### Implemented
- ✅ MCP Tool Registry
- ✅ MCP Tool Definitions
- ✅ Tool Execution Service
- ✅ LangChain4j Adapter
- ✅ Spring AI Integration Points
- ✅ Tool Discovery

### Future Enhancements
- 🔄 Spring AI MCP Server Auto-configuration (when available)
- 🔄 MCP Client for External Tools
- 🔄 Tool Versioning
- 🔄 Tool Caching
- 🔄 Tool Analytics

## Usage Examples

### Using MCP Tools with LangChain4j

```java
@Autowired
private McpToolAdapter adapter;

// Get tools
List<ToolSpecification> tools = adapter.getLangChain4jTools();

// Execute tool
String result = adapter.executeMcpTool("validate_customer", 
    Map.of("customerId", "CUST123"));
```

### Using MCP Tools with Spring AI

```java
@Autowired
private SpringAiToolService service;

// Get available tools
List<McpTool> tools = service.getAvailableTools();

// Execute tool
Object result = service.executeMcpTool("validate_account",
    Map.of("accountNumber", "ACC123", "customerId", "CUST123"));
```

## Summary

The framework now supports:
- **Spring AI 2.0.0-M1**: Latest Spring AI with auto-configuration
- **MCP Protocol**: Standardized tool interface
- **LangChain4j**: Primary LLM integration
- **LangGraph**: State machine orchestration
- **Hybrid Approach**: Multiple AI frameworks working together

This provides maximum flexibility and future-proofing while maintaining compatibility with existing code.
