# MCP Tools Integration Guide

This document explains how MCP (Model Context Protocol) tools are integrated into the credit refund agent framework.

## What is MCP?

Model Context Protocol (MCP) is a standardized protocol for exposing tools and context to language models. It provides:

- **Standardized Interface**: Consistent way to define and expose tools
- **Tool Discovery**: LLMs can automatically discover available tools
- **Interoperability**: Works across different LLM providers and frameworks
- **Type Safety**: JSON schema definitions for tool parameters

## MCP Tools in This Framework

### Tool Registry

The `McpToolRegistry` manages all MCP-compliant tools:

```java
@Autowired
private McpToolRegistry mcpToolRegistry;

// Get all available tools
List<McpTool> tools = mcpToolRegistry.getValidationTools();

// Execute a tool
Object result = mcpToolRegistry.executeTool("validate_customer", 
    Map.of("customerId", "CUST123"));
```

### Available Tools

#### Mandatory Validation Tools

1. **validate_customer**
   - Validates customer information
   - Checks risk markers
   - Returns: CustomerValidationResult

2. **validate_account**
   - Validates account details
   - Checks repayment history
   - Returns: AccountValidationResult

3. **validate_card**
   - Validates card details
   - Checks fraud indicators
   - Returns: CardValidationResult

#### Optional Analysis Tools

4. **get_customer_transaction_history**
   - Gets transaction history
   - Parameters: customerId, months (default: 6)
   - Returns: Transaction history summary

5. **check_similar_refunds**
   - Checks for similar refund patterns
   - Parameters: accountNumber, days (default: 30)
   - Returns: Similar refund analysis

6. **get_account_activity_summary**
   - Gets account activity details
   - Parameters: accountNumber, days (default: 30)
   - Returns: Activity summary

## Integration with LangChain4j

MCP tools are converted to LangChain4j tool specifications:

```java
@Autowired
private McpToolAdapter adapter;

// Get LangChain4j tool specifications
List<ToolSpecification> tools = adapter.getLangChain4jTools();

// Execute tool
String result = adapter.executeMcpTool("validate_customer",
    Map.of("customerId", "CUST123"));
```

## Integration with Spring AI

MCP tools can be used with Spring AI ChatClient:

```java
@Autowired
private SpringAiToolService service;

// Get available tools
List<McpTool> tools = service.getAvailableTools();

// Execute tool
Object result = service.executeMcpTool("validate_account",
    Map.of("accountNumber", "ACC123", "customerId", "CUST123"));
```

## Tool Definition Structure

Each MCP tool is defined with:

```java
McpTool.builder()
    .name("tool_name")                    // Unique identifier
    .description("What the tool does")    // LLM-readable description
    .inputSchema(schema)                  // JSON schema for parameters
    .build();
```

### Input Schema Example

```java
Map<String, Object> schema = new HashMap<>();
schema.put("type", "object");
Map<String, Object> properties = new HashMap<>();
properties.put("customerId", Map.of(
    "type", "string",
    "description", "Customer ID to validate"
));
schema.put("properties", properties);
schema.put("required", List.of("customerId"));
```

## Tool Execution

Tools are executed through the registry:

```java
// Direct execution
Object result = mcpToolRegistry.executeTool("validate_customer",
    Map.of("customerId", "CUST123"));

// Via adapter (for LangChain4j)
String result = mcpToolAdapter.executeMcpTool("validate_customer",
    Map.of("customerId", "CUST123"));

// Via Spring AI service
Object result = springAiToolService.executeMcpTool("validate_customer",
    Map.of("customerId", "CUST123"));
```

## Adding New MCP Tools

### Step 1: Implement Tool Logic

```java
@Component
public class MyValidationTool {
    public MyResult validateSomething(String param) {
        // Your validation logic
        return new MyResult();
    }
}
```

### Step 2: Register in McpToolRegistry

```java
// In getValidationTools()
Map<String, Object> schema = createSchema();
tools.add(McpTool.builder()
    .name("validate_something")
    .description("Validates something important")
    .inputSchema(schema)
    .build());
```

### Step 3: Add Execution Logic

```java
// In executeTool()
case "validate_something":
    String param = (String) arguments.get("param");
    return myTool.validateSomething(param);
```

## Tool Usage in Agents

Agents can use MCP tools through:

1. **Direct Registry Access**: `mcpToolRegistry.executeTool()`
2. **LangChain4j Adapter**: `mcpToolAdapter.executeMcpTool()`
3. **Spring AI Service**: `springAiToolService.executeMcpTool()`

The framework automatically makes MCP tools available to LLMs when:
- Tool calling is enabled (`enableOptionalTools() = true`)
- Agent uses `AgentToolService`
- Tools are registered in `McpToolRegistry`

## Benefits of MCP

1. **Standardization**: Tools follow MCP specification
2. **Discoverability**: LLMs can discover tools automatically
3. **Interoperability**: Works with any MCP-compliant client
4. **Documentation**: Schema provides self-documenting tools
5. **Type Safety**: JSON schema ensures correct parameters

## Future Enhancements

- MCP Server Auto-configuration (when Spring AI 2.0.0-M1 is available)
- MCP Client for External Tools
- Tool Versioning
- Tool Caching
- Tool Usage Analytics
- Dynamic Tool Registration

## Summary

MCP tools provide a standardized way to expose validation and analysis tools to LLMs. The framework supports:

- ✅ MCP Tool Registry
- ✅ Tool Execution Service
- ✅ LangChain4j Integration
- ✅ Spring AI Integration Points
- ✅ Tool Discovery
- ✅ Schema-based Tool Definitions

This enables LLMs to discover and use tools in a standardized, interoperable way.
