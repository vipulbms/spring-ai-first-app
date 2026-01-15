# Quick Start Guide

## Prerequisites Check

1. **Java 21**: `java -version` should show version 21 or higher
2. **Maven**: `mvn -version` should show Maven 3.6+
3. **Ollama**: `ollama --version` should be installed

## Setup Steps

### 1. Install and Start Ollama

```bash
# Install Ollama (if not installed)
# macOS:
brew install ollama

# Or download from https://ollama.ai

# Pull Llama3.2 model
ollama pull llama3.2

# Start Ollama server (runs on http://localhost:11434 by default)
ollama serve
```

### 2. Build the Project

```bash
cd 7_spring_ai_java
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Test the API

#### Health Check
```bash
curl http://localhost:8080/api/refund/health
```

#### Process a Refund Request
```bash
curl -X POST http://localhost:8080/api/refund/process \
  -H "Content-Type: application/json" \
  -d @EXAMPLE_REQUEST.json
```

Or using the inline JSON:
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

## Expected Flow (LangGraph State Machine)

1. **Maker Agent** reviews the request (State: PENDING_REVIEW)
2. **Checker Agent** validates the maker's decision (State: APPROVED/REJECTED)
3. **Fulfillment Agent** processes the transaction if approved (State: FULFILLED)
4. All actions are logged asynchronously to the audit log
5. State transitions are managed by the LangGraph state machine

## Troubleshooting

### Ollama Connection Issues
- Ensure Ollama is running: `ollama list`
- Check Ollama URL in `application.yml`: `langchain4j.ollama.base-url`
- Verify model is available: `ollama list` should show `llama3.2`

### Port Already in Use
- Change port in `application.yml`: `server.port: 8081`

### Build Issues
- Clean and rebuild: `mvn clean install -U`
- Check Java version: Must be Java 21

## Next Steps

- Review the README.md for detailed documentation
- Check the code structure in `src/main/java/com/financial/agents/framework/`
- Explore the LangGraph state machine in `graph/RefundStateGraph.java`
- Extend the framework by creating new agents (see README for examples)
- Customize the state graph by adding new nodes and edges

## Key Technologies

- **LangChain4j**: Java port of LangChain for LLM integration
- **Custom StateGraph**: LangGraph-inspired state machine for agent orchestration
- **Ollama**: Local LLM runtime with Llama3.2
