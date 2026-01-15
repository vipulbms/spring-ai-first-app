# Tools, Risk Assessment, and Memory Management

This document describes the validation tools, risk assessment capabilities, and memory management features added to the credit refund agent framework.

## Overview

The framework now includes:
- **Validation Tools**: Customer, Card, and Account validation with risk marker detection
- **Risk Assessment Tool**: Comprehensive risk analysis using LLM reasoning
- **Context Management**: Centralized context building and validation data aggregation
- **Memory Management**: Conversation memory using LangChain4j's ChatMemory

## Validation Tools

### 1. CustomerValidationTool

Validates customer information and checks for risk markers.

**Methods:**
- `validateCustomer(String customerId)`: Validates customer and returns risk score
- `getCustomerDetails(String customerId)`: Retrieves customer details

**Returns:**
- Customer validity and active status
- Risk score (0-100, lower is better)
- Risk markers (fraud flags, suspicious activity, compliance issues)
- Account status and customer since date

### 2. CardValidationTool

Validates card details and checks for fraud indicators.

**Methods:**
- `validateCard(String accountNumber, String cardNumber)`: Validates card
- `getCardDetails(String accountNumber)`: Retrieves card details

**Returns:**
- Card validity, active status, and expiration
- Risk indicators (stolen reports, fraud patterns, unusual activity)
- Recent transaction count

### 3. AccountValidationTool

Validates account details, repayment history, and account validity.

**Methods:**
- `validateAccount(String accountNumber, String customerId)`: Validates account
- `getRepaymentHistory(String accountNumber)`: Gets repayment history
- `getAccountDetails(String accountNumber)`: Retrieves account details

**Returns:**
- Account validity and status
- Balance and credit limit
- Repayment history (on-time, late, missed payments)
- Payment history score (0-100, higher is better)
- Risk markers (overdue payments, high utilization, account restrictions)

## Risk Assessment Tool

### RiskAssessmentTool

Performs comprehensive risk assessment using LLM reasoning on all validation data.

**Method:**
- `assessRisk(RefundRequest request, Map<String, Object> validationData)`: Comprehensive risk assessment

**Process:**
1. Gathers all validation data (customer, card, account)
2. Builds risk context with all relevant information
3. Uses LLM to analyze risk with detailed reasoning
4. Calculates risk score (0-100)
5. Determines risk level (LOW, MODERATE, HIGH)
6. Generates recommendation (APPROVE, REVIEW, REJECT)

**Returns:**
- Risk score and risk level
- Detailed LLM risk analysis
- Aggregated risk markers
- Recommendation based on risk assessment

## Context Management

### ContextService

Centralized service for building comprehensive context and managing validation data.

**Method:**
- `buildContext(RefundRequest request)`: Builds complete context with all validations

**What it does:**
1. Validates customer using CustomerValidationTool
2. Validates account using AccountValidationTool
3. Validates card using CardValidationTool
4. Performs risk assessment using RiskAssessmentTool
5. Builds validation summary
6. Returns comprehensive context map

**Context includes:**
- `customerValidation`: CustomerValidationResult
- `customerDetails`: Customer details map
- `accountValidation`: AccountValidationResult
- `accountDetails`: Account details map
- `repaymentHistory`: RepaymentHistory object
- `cardValidation`: CardValidationResult
- `cardDetails`: Card details map
- `riskAssessment`: RiskAssessmentResult
- `validationSummary`: Summary map

## Memory Management

### MemoryService

Manages conversation memory for agents using LangChain4j's ChatMemory.

**Features:**
- Per-request memory storage
- Message window (last 50 messages per session)
- Automatic memory creation
- Memory cleanup capabilities

**Methods:**
- `getOrCreateMemory(String requestId)`: Get or create memory for request
- `getMemory(String requestId)`: Get existing memory
- `clearMemory(String requestId)`: Clear memory for request
- `clearAllMemories()`: Clear all memories
- `hasMemory(String requestId)`: Check if memory exists

**Usage in Agents:**
- BaseAgent automatically uses memory for LLM interactions
- Each agent's reasoning is stored in memory
- Context is maintained across agent interactions
- Memory persists for the duration of the request

## Integration with Agents

### MakerAgent Enhancements

The MakerAgent now:
1. Uses ContextService to build comprehensive context
2. Performs all validations (customer, account, card)
3. Gets risk assessment with LLM reasoning
4. Uses memory for context-aware LLM interactions
5. Considers risk markers in decision making
6. Reviews repayment history
7. Checks account validity

**Decision Logic:**
- High risk → Automatic rejection
- Moderate risk → Requires strong LLM approval
- Low risk → Standard approval process
- Considers payment history, overdue amounts, fraud flags

### BaseAgent Memory Integration

All agents now:
- Use ChatMemory for conversation context
- Maintain memory across interactions
- Include validation data in prompts
- Generate context-aware reasoning

## Example Flow

```
1. Request received
   ↓
2. ContextService.buildContext()
   ├─→ CustomerValidationTool.validateCustomer()
   ├─→ AccountValidationTool.validateAccount()
   ├─→ CardValidationTool.validateCard()
   └─→ RiskAssessmentTool.assessRisk()
   ↓
3. MakerAgent receives context
   ├─→ Uses memory for LLM interaction
   ├─→ Reviews validation data
   ├─→ Considers risk assessment
   └─→ Makes decision with reasoning
   ↓
4. CheckerAgent reviews
   ├─→ Uses memory (includes maker's reasoning)
   ├─→ Validates maker's decision
   └─→ Makes final decision
   ↓
5. FulfillmentAgent (if approved)
   ├─→ Uses memory (includes all previous context)
   └─→ Processes transaction
```

## Configuration

No additional configuration required. All tools and services are auto-configured via Spring dependency injection.

## Production Considerations

1. **Database Integration**: Replace simulated validation with actual database queries
2. **Caching**: Cache validation results to reduce database load
3. **Memory Persistence**: Consider persisting memory for audit purposes
4. **Risk Model**: Replace simplified risk scoring with ML-based risk models
5. **Real-time Data**: Integrate with real-time fraud detection systems
6. **Performance**: Optimize validation tool calls (parallel execution)
7. **Monitoring**: Add metrics for validation tool performance and risk scores

## Extending the Framework

### Adding New Validation Tools

1. Create a new tool class (e.g., `TransactionValidationTool`)
2. Implement validation methods
3. Add to ContextService.buildContext()
4. Update agents to use new validation data

### Customizing Risk Assessment

1. Modify RiskAssessmentTool.assessRisk()
2. Update risk score calculation logic
3. Adjust risk level thresholds
4. Enhance LLM prompt for better analysis

### Memory Customization

1. Adjust message window size in MemoryService
2. Implement persistent memory storage
3. Add memory summarization for long conversations
4. Customize memory cleanup policies
