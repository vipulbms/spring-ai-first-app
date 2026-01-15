package com.financial.agents.framework.graph;

import com.financial.agents.framework.agent.CheckerAgent;
import com.financial.agents.framework.agent.FulfillmentAgent;
import com.financial.agents.framework.agent.MakerAgent;
import com.financial.agents.framework.domain.AgentResponse;
import com.financial.agents.framework.domain.RefundState;
import com.financial.agents.framework.domain.RefundStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundStateGraph {
    
    private final MakerAgent makerAgent;
    private final CheckerAgent checkerAgent;
    private final FulfillmentAgent fulfillmentAgent;
    
    private StateGraph<RefundState> graph;
    
    public void initialize() {
        StateGraph.StateGraphBuilder<RefundState> builder = StateGraph.builder(RefundState.class);
        
        // Define nodes (agent execution steps)
        builder.addNode("maker", this::executeMaker);
        builder.addNode("checker", this::executeChecker);
        builder.addNode("fulfillment", this::executeFulfillment);
        builder.addNode("reject", this::handleRejection);
        
        // Define edges (flow control)
        builder.setEntryPoint("maker");
        
        // From maker: if approved, go to checker; if rejected, go to reject
        builder.addConditionalEdges("maker", this::afterMaker, 
            Map.of("approved", "checker", "rejected", "reject"));
        
        // From checker: if approved, go to fulfillment; if rejected, go to reject
        builder.addConditionalEdges("checker", this::afterChecker,
            Map.of("approved", "fulfillment", "rejected", "reject"));
        
        // From fulfillment: end (no more edges needed)
        builder.addEdge("fulfillment", StateGraph.END);
        builder.addEdge("reject", StateGraph.END);
        
        this.graph = builder.build();
    }
    
    public RefundState execute(RefundState initialState) {
        if (graph == null) {
            initialize();
        }
        return graph.invoke(initialState);
    }
    
    private RefundState executeMaker(RefundState state) {
        log.info("Executing Maker agent for request: {}", state.getRequest().getRequestId());
        Map<String, Object> context = new HashMap<>(state.getContext());
        
        AgentResponse makerResponse = makerAgent.execute(state.getRequest(), context);
        state.setMakerResponse(makerResponse);
        state.addToContext("makerResponse", makerResponse);
        
        if (makerResponse.isApproved()) {
            state.setCurrentStatus(RefundStatus.PENDING_REVIEW);
        } else {
            state.setCurrentStatus(RefundStatus.REJECTED);
        }
        
        return state;
    }
    
    private RefundState executeChecker(RefundState state) {
        log.info("Executing Checker agent for request: {}", state.getRequest().getRequestId());
        Map<String, Object> context = new HashMap<>(state.getContext());
        
        AgentResponse checkerResponse = checkerAgent.execute(state.getRequest(), context);
        state.setCheckerResponse(checkerResponse);
        state.addToContext("checkerResponse", checkerResponse);
        
        if (checkerResponse.isApproved()) {
            state.setCurrentStatus(RefundStatus.APPROVED);
        } else {
            state.setCurrentStatus(RefundStatus.REJECTED);
        }
        
        return state;
    }
    
    private RefundState executeFulfillment(RefundState state) {
        log.info("Executing Fulfillment agent for request: {}", state.getRequest().getRequestId());
        Map<String, Object> context = new HashMap<>(state.getContext());
        
        AgentResponse fulfillmentResponse = fulfillmentAgent.execute(state.getRequest(), context);
        state.setFulfillmentResponse(fulfillmentResponse);
        
        if (fulfillmentResponse.isApproved()) {
            state.setCurrentStatus(RefundStatus.FULFILLED);
        } else {
            state.setCurrentStatus(RefundStatus.FAILED);
        }
        
        return state;
    }
    
    private RefundState handleRejection(RefundState state) {
        log.info("Handling rejection for request: {}", state.getRequest().getRequestId());
        state.setCurrentStatus(RefundStatus.REJECTED);
        if (state.getMakerResponse() != null && !state.getMakerResponse().isApproved()) {
            state.setErrorMessage("Rejected by Maker Agent: " + state.getMakerResponse().getReasoning());
        } else if (state.getCheckerResponse() != null && !state.getCheckerResponse().isApproved()) {
            state.setErrorMessage("Rejected by Checker Agent: " + state.getCheckerResponse().getReasoning());
        }
        return state;
    }
    
    private String afterMaker(RefundState state) {
        if (state.getMakerResponse() != null && state.getMakerResponse().isApproved()) {
            return "approved";
        }
        return "rejected";
    }
    
    private String afterChecker(RefundState state) {
        if (state.getCheckerResponse() != null && state.getCheckerResponse().isApproved()) {
            return "approved";
        }
        return "rejected";
    }
}
