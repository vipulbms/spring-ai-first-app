# Optional LLM Tool Calling

This document explains the optional tool calling feature that allows LLMs to call validation tools when they need additional information beyond the mandatory validations.

## Overview

The framework now supports **two-tier validation**:

1. **Mandatory Validations** (Pre-executed): Always run upfront via `ContextService`
   - Customer validation
   - Account validation  
   - Card validation
   - Risk assessment

2. **Optional Tool Calling** (LLM-driven): LLM can call additional tools if needed
   - Transaction history analysis
   - Similar refund pattern checking
   - Deep risk marker analysis
   - Customer relationship history
   - And more...

## Architecture

### How It Works

```
1. Mandatory Validations (ContextService)
   ↓
2. LLM receives validation data + optional tools
   ↓
3. LLM can call optional tools if needed
   ↓
4. LLM generates reasoning with all available data
```

### Components

#### 1. OptionalValidationTools

A Spring component with methods annotated with `@Tool` that LLMs can call:

```java
@Tool("Get detailed customer transaction history...")
public String getCustomerTransactionHistory(String customerId, int months) {
    // Returns transaction history
}
```

**Available Tools:**
- `getCustomerTransactionHistory`: Transaction history analysis
- `checkSimilarRefunds`: Pattern detection for similar refunds
- `getAccountActivitySummary`: Account activity details
- `analyzeRiskMarkers`: Deep dive risk analysis
- `getCustomerRelationshipHistory`: Customer relationship metrics
- `validateCardDetails`: Additional card validation
- `getDetailedRepaymentAnalysis`: Comprehensive repayment analysis

#### 2. AgentToolService

Service that creates AI service interfaces with tool calling support:

```java
AgentWithToolsInterface agent = AiServices.builder(AgentWithToolsInterface.class)
    .chatLanguageModel(chatModel)
    .chatMemory(memory)
    .tools(optionalTools)  // Makes tools available to LLM
    .build();
```

#### 3. BaseAgent Enhancement

BaseAgent now supports optional tool calling:

- `enableOptionalTools()`: Override to enable/disable per agent
- Automatic tool integration via `AgentToolService`
- Fallback to standard reasoning if tool calling fails

## Usage

### Enabling Tool Calling in Agents

```java
@Override
protected boolean enableOptionalTools() {
    return true; // Enable for this agent
}
```

### Current Configuration

- **MakerAgent**: ✅ Tool calling enabled
- **CheckerAgent**: ✅ Tool calling enabled  
- **FulfillmentAgent**: ❌ Tool calling disabled (not needed)

### System Prompt Integration

Agents with tool calling enabled receive a system prompt that mentions available tools:

```
OPTIONAL TOOLS AVAILABLE:
If you need additional information, you can use these optional tools:
- getCustomerTransactionHistory: Get detailed transaction history...
- checkSimilarRefunds: Check for similar refund patterns...
...
```

## Example Flow

### Scenario: LLM Needs Transaction History

1. **Mandatory validations run** (customer, account, card, risk)
2. **LLM receives** validation data + system prompt with tools
3. **LLM decides** it needs transaction history
4. **LLM calls** `getCustomerTransactionHistory(customerId, 6)`
5. **Tool executes** and returns transaction data
6. **LLM receives** tool result and continues reasoning
7. **LLM generates** final decision with transaction context

### Code Flow

```java
// In MakerAgent.processRequest()
Map<String, Object> fullContext = contextService.buildContext(request); // Mandatory

// LLM reasoning with optional tools
String reasoning = generateReasoning(request, additionalContext, validationData);
// ↑ This can now call optional tools if enableOptionalTools() returns true
```

## Benefits

### 1. **Flexibility**
- LLM decides what additional information it needs
- Not forced to run all possible validations
- Can focus on specific concerns

### 2. **Efficiency**
- Mandatory validations ensure compliance
- Optional tools only called when needed
- Reduces unnecessary processing

### 3. **Intelligence**
- LLM can reason about what it needs
- Can follow up on specific risk markers
- Can investigate anomalies

### 4. **Extensibility**
- Easy to add new optional tools
- Just add `@Tool` annotated methods
- Automatically available to LLMs

## Adding New Optional Tools

### Step 1: Add Tool Method

```java
@Component
public class OptionalValidationTools {
    
    @Tool("Description of what this tool does...")
    public String myNewTool(String param1, int param2) {
        // Implementation
        return "Tool result";
    }
}
```

### Step 2: Tool is Automatically Available

The tool is automatically available to all agents with `enableOptionalTools() = true`.

### Step 3: Update System Prompt (Optional)

Update agent system prompts to mention the new tool if needed.

## Configuration

### Enabling/Disabling Per Agent

```java
@Override
protected boolean enableOptionalTools() {
    return true;  // Enable
    // return false; // Disable (default)
}
```

### Global Configuration

You can add a configuration property to control tool calling globally:

```yaml
agent:
  optional-tools:
    enabled: true
    max-tool-calls: 5  # Limit tool calls per request
```

## Best Practices

### 1. **Tool Descriptions**
- Write clear, descriptive `@Tool` annotations
- Explain when to use the tool
- Describe what it returns

### 2. **Tool Performance**
- Keep tools fast (avoid slow database queries)
- Consider caching results
- Log tool calls for monitoring

### 3. **Error Handling**
- Tools should handle errors gracefully
- Return meaningful error messages
- Don't throw exceptions that break the flow

### 4. **Tool Results**
- Return structured, readable results
- Include relevant context
- Format for LLM consumption

## Monitoring

### Tool Call Logging

All tool calls are logged:

```java
log.info("LLM called: getCustomerTransactionHistory for customer: {}, months: {}", customerId, months);
```

### Audit Trail

Tool calls can be added to audit logs:

```java
auditService.logAction(agentType, agentName, "TOOL_CALLED", 
    "Tool: " + toolName, requestId, context);
```

## Limitations

### 1. **Model Support**
- Requires LLM that supports function/tool calling
- Ollama/Llama3.2 may have limited support
- Consider using OpenAI or other providers for better tool calling

### 2. **Tool Execution**
- Tools execute synchronously
- Multiple tool calls are sequential
- Consider async execution for performance

### 3. **Error Recovery**
- Falls back to standard reasoning on errors
- Tool failures don't break the flow
- May need retry logic for critical tools

## Future Enhancements

1. **Parallel Tool Execution**: Execute multiple tools concurrently
2. **Tool Result Caching**: Cache tool results to avoid duplicate calls
3. **Tool Call Limits**: Enforce maximum tool calls per request
4. **Tool Call Analytics**: Track which tools are used most
5. **Dynamic Tool Registration**: Add/remove tools at runtime
6. **Tool Result Summarization**: Summarize tool results for context

## Summary

Optional tool calling provides a powerful way to give LLMs access to additional validation tools while maintaining the mandatory validation requirements. This hybrid approach combines:

- **Compliance**: Mandatory validations ensure all required checks
- **Intelligence**: LLM can investigate specific concerns
- **Efficiency**: Tools only called when needed
- **Flexibility**: Easy to add new tools

The framework now supports both deterministic mandatory validations and intelligent optional tool calling, providing the best of both worlds for financial institution workflows.
