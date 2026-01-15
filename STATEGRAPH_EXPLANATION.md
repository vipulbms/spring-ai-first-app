# StateGraph and RefundStateGraph Explanation

This document explains the `StateGraph` (generic state machine) and `RefundStateGraph` (refund-specific implementation) classes that orchestrate the agent workflow.

## Overview

The framework uses a **state machine pattern** inspired by LangGraph to orchestrate the three-agent workflow (Maker → Checker → Fulfillment). This provides:
- **Clear workflow definition**: Visual representation of the process flow
- **Conditional routing**: Dynamic path selection based on agent decisions
- **State management**: Centralized state that flows through the workflow
- **Extensibility**: Easy to add new nodes or modify the flow

## StateGraph (Generic State Machine)

### Purpose

`StateGraph<T>` is a **generic, reusable state machine** that can orchestrate any workflow. It's type-agnostic and works with any state type `T`.

### Key Components

#### 1. **Nodes** (Execution Steps)
```java
Map<String, Function<T, T>> nodes
```
- **What**: Named functions that transform state
- **How**: Each node is a `Function<T, T>` that takes state, processes it, and returns updated state
- **Example**: A node might execute an agent and update the state with the agent's response

#### 2. **Edges** (Flow Control)
```java
Map<String, Map<String, String>> edges
```
- **What**: Defines where to go after each node
- **How**: Maps from node name → route name → next node name
- **Types**:
  - **Simple edges**: Always go to the same next node
  - **Conditional edges**: Route based on state evaluation

#### 3. **Conditional Edges** (Dynamic Routing)
```java
Map<String, Function<T, String>> conditionalEdges
```
- **What**: Functions that evaluate state and return a route name
- **How**: Takes current state, returns a string (route name) that maps to the next node
- **Example**: "approved" → goes to next agent, "rejected" → goes to rejection handler

#### 4. **Entry Point**
```java
String entryPoint
```
- **What**: The starting node of the workflow
- **How**: First node executed when `invoke()` is called

### Execution Flow

```
1. Start at entryPoint
   ↓
2. Execute node function with current state
   ↓
3. Update state with node's return value
   ↓
4. Determine next node:
   - If conditional edge exists → evaluate condition → route
   - Else if simple edge exists → follow default route
   - Else → END
   ↓
5. Repeat until END is reached
   ↓
6. Return final state
```

### Builder Pattern

The `StateGraphBuilder` provides a fluent API:

```java
StateGraph.builder(RefundState.class)
    .addNode("node1", this::executeNode1)
    .addNode("node2", this::executeNode2)
    .setEntryPoint("node1")
    .addEdge("node1", "node2")
    .addConditionalEdges("node2", this::evaluateCondition, 
        Map.of("route1", "node3", "route2", "node4"))
    .build();
```

### Example Usage

```java
// Create a simple workflow
StateGraph<String> graph = StateGraph.builder(String.class)
    .addNode("start", s -> s + " -> processed")
    .addNode("end", s -> s + " -> completed")
    .setEntryPoint("start")
    .addEdge("start", "end")
    .addEdge("end", StateGraph.END)
    .build();

// Execute
String result = graph.invoke("Initial");
// Result: "Initial -> processed -> completed"
```

---

## RefundStateGraph (Refund-Specific Implementation)

### Purpose

`RefundStateGraph` is a **specialized implementation** that orchestrates the credit refund process using the three-agent pattern (Maker-Checker-Fulfillment).

### Workflow Structure

```
                    [START]
                      ↓
                  [maker] ← Entry Point
                      ↓
            ┌─────────┴─────────┐
            │                   │
      [approved]          [rejected]
            │                   │
        [checker]          [reject]
            ↓                   ↓
    ┌───────┴───────┐       [END]
    │               │
[approved]    [rejected]
    │               │
[fulfillment]   [reject]
    ↓               ↓
  [END]           [END]
```

### Node Definitions

#### 1. **maker Node**
```java
private RefundState executeMaker(RefundState state)
```
- **Purpose**: Execute Maker Agent to review the refund request
- **Actions**:
  - Calls `makerAgent.execute()` with request and context
  - Stores `makerResponse` in state
  - Updates status to `PENDING_REVIEW` (if approved) or `REJECTED`
- **Next**: Conditional routing based on approval

#### 2. **checker Node**
```java
private RefundState executeChecker(RefundState state)
```
- **Purpose**: Execute Checker Agent to validate Maker's decision
- **Actions**:
  - Calls `checkerAgent.execute()` with request and context (includes makerResponse)
  - Stores `checkerResponse` in state
  - Updates status to `APPROVED` (if approved) or `REJECTED`
- **Next**: Conditional routing based on approval

#### 3. **fulfillment Node**
```java
private RefundState executeFulfillment(RefundState state)
```
- **Purpose**: Execute Fulfillment Agent to process the transaction
- **Actions**:
  - Calls `fulfillmentAgent.execute()` with request and context
  - Stores `fulfillmentResponse` in state
  - Updates status to `FULFILLED` (if successful) or `FAILED`
- **Next**: Always goes to END

#### 4. **reject Node**
```java
private RefundState handleRejection(RefundState state)
```
- **Purpose**: Handle rejection scenarios
- **Actions**:
  - Sets status to `REJECTED`
  - Sets error message based on which agent rejected
- **Next**: Always goes to END

### Conditional Routing Functions

#### 1. **afterMaker**
```java
private String afterMaker(RefundState state)
```
- **Evaluates**: Maker agent's approval decision
- **Returns**: 
  - `"approved"` → routes to `checker` node
  - `"rejected"` → routes to `reject` node

#### 2. **afterChecker**
```java
private String afterChecker(RefundState state)
```
- **Evaluates**: Checker agent's approval decision
- **Returns**:
  - `"approved"` → routes to `fulfillment` node
  - `"rejected"` → routes to `reject` node

### State Flow Example

Let's trace through a successful refund request:

```
Initial State:
{
  request: RefundRequest{...},
  makerResponse: null,
  checkerResponse: null,
  fulfillmentResponse: null,
  currentStatus: PENDING_REVIEW,
  context: {}
}

↓ [maker node executes]

State after maker:
{
  request: RefundRequest{...},
  makerResponse: AgentResponse{approved: true, ...},
  checkerResponse: null,
  fulfillmentResponse: null,
  currentStatus: PENDING_REVIEW,
  context: {makerResponse: ...}
}

↓ [afterMaker returns "approved"] → routes to checker

↓ [checker node executes]

State after checker:
{
  request: RefundRequest{...},
  makerResponse: AgentResponse{approved: true, ...},
  checkerResponse: AgentResponse{approved: true, ...},
  fulfillmentResponse: null,
  currentStatus: APPROVED,
  context: {makerResponse: ..., checkerResponse: ...}
}

↓ [afterChecker returns "approved"] → routes to fulfillment

↓ [fulfillment node executes]

Final State:
{
  request: RefundRequest{...},
  makerResponse: AgentResponse{approved: true, ...},
  checkerResponse: AgentResponse{approved: true, ...},
  fulfillmentResponse: AgentResponse{approved: true, ...},
  currentStatus: FULFILLED,
  context: {makerResponse: ..., checkerResponse: ..., fulfillmentResponse: ...}
}

↓ [routes to END]
```

### Rejection Flow Example

If Maker rejects:

```
Initial State → [maker] → State: {makerResponse: {approved: false}}
↓
[afterMaker returns "rejected"] → routes to reject
↓
[reject node] → State: {currentStatus: REJECTED, errorMessage: "Rejected by Maker Agent: ..."}
↓
[END]
```

### Key Features

1. **Lazy Initialization**: Graph is built on first `execute()` call
2. **State Accumulation**: Each node adds to state (responses, context, status)
3. **Context Passing**: Context map is passed to each agent, accumulating data
4. **Error Handling**: Rejection node captures error messages
5. **Status Tracking**: Current status updated at each step

### Integration with Agents

Each node:
1. Extracts context from state
2. Calls the appropriate agent with request and context
3. Updates state with agent response
4. Adds response to context for next agents
5. Updates status

This ensures:
- **Context continuity**: Each agent sees previous agents' decisions
- **State persistence**: All agent responses are preserved
- **Traceability**: Complete audit trail in final state

## Benefits of This Architecture

### 1. **Separation of Concerns**
- `StateGraph`: Generic, reusable state machine
- `RefundStateGraph`: Business logic specific to refunds
- Agents: Focus on their specific validation/processing

### 2. **Extensibility**
- Easy to add new nodes (e.g., "compliance" node)
- Easy to modify routing logic
- Easy to add new conditional paths

### 3. **Testability**
- Each node can be tested independently
- State transitions are explicit and verifiable
- Mock agents can be injected for testing

### 4. **Maintainability**
- Clear visual representation of workflow
- Centralized flow control
- Easy to understand and modify

### 5. **Debugging**
- State at each step is visible
- Can log state transitions
- Easy to trace execution path

## Extending the Graph

### Adding a New Node

```java
// 1. Add node execution method
private RefundState executeCompliance(RefundState state) {
    // Execute compliance agent
    AgentResponse complianceResponse = complianceAgent.execute(...);
    state.setComplianceResponse(complianceResponse);
    return state;
}

// 2. Add to graph
builder.addNode("compliance", this::executeCompliance);

// 3. Add routing
builder.addConditionalEdges("checker", this::afterChecker,
    Map.of("approved", "compliance", "rejected", "reject"));
builder.addEdge("compliance", "fulfillment");
```

### Modifying Routing Logic

```java
// Add more sophisticated routing
private String afterMaker(RefundState state) {
    if (state.getMakerResponse().isApproved()) {
        // Check if amount requires additional review
        if (state.getRequest().getRefundAmount().compareTo(THRESHOLD) > 0) {
            return "highValue"; // Route to special handling
        }
        return "approved";
    }
    return "rejected";
}
```

## Summary

- **StateGraph**: Generic, reusable state machine for any workflow
- **RefundStateGraph**: Specialized implementation for refund process
- **Workflow**: Maker → Checker → Fulfillment with conditional routing
- **State Management**: Centralized state that accumulates through the workflow
- **Extensibility**: Easy to add nodes, modify routing, or change flow

This architecture provides a clean, maintainable way to orchestrate complex multi-agent workflows while keeping the code organized and testable.
