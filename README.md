# Credit Refund Agent Framework

A reusable and extensible agent framework built with **Spring AI 2.0.0-M1**, **LangChain4j**, **MCP (Model Context Protocol)**, and **LangGraph** (custom implementation) for Java 21 and Spring Boot, designed for financial institutions to process credit balance refund requests. The framework implements a three-agent orchestration pattern (Maker-Checker-Fulfillment) with comprehensive audit logging using a state machine approach.

## Features

- **Three-Agent Orchestration Pattern**:
  - **Maker Agent**: Reviews and validates customer refund requests
  - **Checker Agent**: Validates maker decisions for compliance and accuracy
  - **Fulfillment Agent**: Executes approved transactions and sends confirmations

- **LangGraph State Machine**: Custom state graph implementation for agent orchestration with conditional routing

- **Asynchronous Audit Logging**: All agent actions are logged asynchronously for performance

- **Extensible Framework**: Base agent class allows easy creation of new agents

- **Spring AI 2.0.0-M1**: Latest Spring AI with auto-configuration and enhanced features

- **MCP (Model Context Protocol)**: Standardized tool protocol for LLM tool calling

- **LangChain4j Integration**: Uses LangChain4j with Ollama and Llama3.2 model for AI-powered decision making

- **Hybrid AI Framework**: Supports both Spring AI and LangChain4j for maximum flexibility

- **RESTful API**: Simple REST endpoints for processing refund requests

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Ollama installed and running with Llama3.2 model

## Setup

### 1. Install Ollama and Llama3.2

```bash
# Install Ollama (macOS)
brew install ollama

# Or download from https://ollama.ai

# Pull Llama3.2 model
ollama pull llama3.2

# Start Ollama server (if not running)
ollama serve
```

### 2. Build the Project

```bash
cd 7_spring_ai_java
mvn clean install
```

### 3. Configure Application

Edit `src/main/resources/application.yml` if needed:

```yaml
langchain4j:
  ollama:
    base-url: http://localhost:11434  # Ollama server URL
    model: llama3.2
    temperature: 0.7
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Usage

### Process a Refund Request

```bash
curl -X POST http://localhost:8080/api/refund/process \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST123",
    "accountNumber": "ACC456789",
    "refundAmount": 500.00,
    "currency": "USD",
    "reason": "Overpayment due to duplicate charge",
    "customerEmail": "customer@example.com",
    "customerPhone": "+1234567890"
  }'
```

### Example Response

```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "FULFILLED",
  "finalMessage": "Refund processed successfully. Transaction processed successfully. Confirmation sent: true",
  "makerResponse": {
    "agentName": "MakerAgent",
    "requestId": "550e8400-e29b-41d4-a716-446655440000",
    "approved": true,
    "reasoning": "DECISION: APPROVE\nREASONING: The refund request is valid...",
    "timestamp": "2024-01-15T10:30:00",
    "agentType": "MAKER"
  },
  "checkerResponse": {
    "agentName": "CheckerAgent",
    "requestId": "550e8400-e29b-41d4-a716-446655440000",
    "approved": true,
    "reasoning": "DECISION: APPROVE\nREASONING: Maker's decision is sound...",
    "timestamp": "2024-01-15T10:30:05",
    "agentType": "CHECKER"
  },
  "fulfillmentResponse": {
    "agentName": "FulfillmentAgent",
    "requestId": "550e8400-e29b-41d4-a716-446655440000",
    "approved": true,
    "reasoning": "Transaction processed successfully...",
    "timestamp": "2024-01-15T10:30:10",
    "agentType": "FULFILLMENT"
  }
}
```

### Health Check

```bash
curl http://localhost:8080/api/refund/health
```

## Architecture

### Agent Framework

The framework is built around a base `BaseAgent` class that provides:

- Common execution flow
- Automatic audit logging
- AI reasoning generation using LangChain4j
- Error handling

### LangGraph State Machine

The orchestration uses a custom `StateGraph` implementation inspired by LangGraph patterns:

- **State-based workflow**: Each step maintains state with request and agent responses
- **Conditional routing**: Flow branches based on agent decisions (approve/reject)
- **Node-based execution**: Each agent is a node in the graph
- **Automatic state transitions**: Graph handles flow control automatically

### Agent Flow (State Graph)

```
Initial State (RefundRequest)
    ↓
[Maker Node] → Review & Validate
    ↓
    ├─→ Approved → [Checker Node] → Validate Decision
    │                    ↓
    │                    ├─→ Approved → [Fulfillment Node] → Execute Transaction
    │                    │                    ↓
    │                    │                    └─→ FULFILLED
    │                    └─→ Rejected → [Reject Node] → REJECTED
    └─→ Rejected → [Reject Node] → REJECTED
```

### Audit Logging

All agent actions are logged asynchronously with:
- Agent type and name
- Action performed
- Request ID
- Timestamp
- Status and details
- Metadata

## Extending the Framework

### Creating a New Agent

1. Extend `BaseAgent`:

```java
@Component
public class CustomAgent extends BaseAgent {
    
    public CustomAgent(ChatClient chatClient, AuditService auditService) {
        super(chatClient, auditService, "CustomAgent", AgentType.CUSTOM);
    }
    
    @Override
    protected String getSystemPrompt() {
        return "Your agent's system prompt...";
    }
    
    @Override
    protected AgentResponse processRequest(RefundRequest request, Map<String, Object> context) {
        // Your agent logic
    }
    
    @Override
    protected String buildPrompt(RefundRequest request, String additionalContext) {
        // Build your prompt
    }
}
```

2. Add the agent to orchestration service

3. Update `AgentType` enum if needed

### Customizing AI Model

Update `application.yml`:

```yaml
langchain4j:
  ollama:
    base-url: http://localhost:11434
    model: your-model-name
    temperature: 0.5
```

## Project Structure

```
src/main/java/com/financial/agents/framework/
├── agent/
│   ├── BaseAgent.java          # Base agent class (LangChain4j)
│   ├── MakerAgent.java         # Maker agent implementation
│   ├── CheckerAgent.java       # Checker agent implementation
│   └── FulfillmentAgent.java   # Fulfillment agent implementation
├── config/
│   ├── AsyncConfig.java        # Async configuration
│   └── LangChain4jConfig.java  # LangChain4j/Ollama configuration
├── controller/
│   └── RefundController.java   # REST API endpoints
├── domain/
│   ├── AgentResponse.java      # Agent response model
│   ├── AgentType.java          # Agent type enum
│   ├── AuditLog.java           # Audit log model
│   ├── RefundRequest.java      # Refund request model
│   ├── RefundState.java        # State graph state model
│   └── RefundStatus.java       # Refund status enum
├── graph/
│   ├── StateGraph.java         # Custom LangGraph-inspired state machine
│   └── RefundStateGraph.java   # Refund process state graph
└── service/
    ├── AuditService.java       # Async audit logging service
    ├── AuditRepository.java    # Audit log repository
    └── RefundOrchestrationService.java  # Orchestration service
```

## Technology Stack

- **Java 21**: Latest LTS version
- **Spring Boot 3.2**: Application framework
- **Spring AI 2.0.0-M1**: Latest Spring AI framework
- **MCP (Model Context Protocol)**: Standardized tool protocol
- **LangChain4j 0.35.0**: Java port of LangChain for LLM integration
- **Ollama**: Local LLM runtime
- **Llama3.2**: Language model
- **Maven**: Build and dependency management
- **Custom StateGraph**: LangGraph-inspired state machine for orchestration

## Production Considerations

1. **Database Integration**: Replace in-memory `AuditRepository` with JPA/MongoDB repository
2. **Structured Output**: Use LangChain4j's structured output for more reliable decision parsing
3. **Transaction Processing**: Integrate with actual payment gateway in `FulfillmentAgent`
4. **Notification Service**: Implement email/SMS sending in `FulfillmentAgent`
5. **Error Handling**: Add retry logic and circuit breakers
6. **Security**: Add authentication, authorization, and input validation
7. **Monitoring**: Add metrics, tracing, and health checks
8. **Configuration**: Externalize all configuration to environment variables
9. **State Persistence**: Add state persistence for long-running workflows
10. **Graph Visualization**: Add visualization tools for state graph debugging

## Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## License

This project is part of the agents framework collection.

## Support

For issues and questions, please refer to the project documentation or contact the development team.
# spring-ai-first-app
