package com.financial.agents.framework.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Simple state graph implementation inspired by LangGraph
 * Provides state machine functionality for agent orchestration
 */
public class StateGraph<T> {
    
    public static final String END = "__END__";
    
    private final Map<String, Function<T, T>> nodes = new HashMap<>();
    private final Map<String, Map<String, String>> edges = new HashMap<>();
    private final Map<String, Function<T, String>> conditionalEdges = new HashMap<>();
    private String entryPoint;
    
    public void addNode(String name, Function<T, T> nodeFunction) {
        nodes.put(name, nodeFunction);
    }
    
    public void setEntryPoint(String nodeName) {
        this.entryPoint = nodeName;
    }
    
    public void addEdge(String from, String to) {
        edges.computeIfAbsent(from, k -> new HashMap<>()).put("default", to);
    }
    
    public void addConditionalEdges(String from, Function<T, String> condition, Map<String, String> routes) {
        conditionalEdges.put(from, condition);
        edges.put(from, routes);
    }
    
    public T invoke(T initialState) {
        String currentNode = entryPoint;
        T currentState = initialState;
        
        while (currentNode != null && !END.equals(currentNode)) {
            Function<T, T> nodeFunction = nodes.get(currentNode);
            if (nodeFunction == null) {
                throw new IllegalStateException("Node not found: " + currentNode);
            }
            
            // Execute the node
            currentState = nodeFunction.apply(currentState);
            
            // Determine next node
            if (conditionalEdges.containsKey(currentNode)) {
                Function<T, String> condition = conditionalEdges.get(currentNode);
                String conditionResult = condition.apply(currentState);
                Map<String, String> routes = edges.get(currentNode);
                currentNode = routes.get(conditionResult);
            } else if (edges.containsKey(currentNode)) {
                Map<String, String> routes = edges.get(currentNode);
                currentNode = routes.get("default");
            } else {
                currentNode = END;
            }
        }
        
        return currentState;
    }
    
    public static <T> StateGraphBuilder<T> builder(Class<T> stateClass) {
        return new StateGraphBuilder<>(stateClass);
    }
    
    public static class StateGraphBuilder<T> {
        private final StateGraph<T> graph;
        
        public StateGraphBuilder(Class<T> stateClass) {
            this.graph = new StateGraph<>();
        }
        
        public StateGraphBuilder<T> addNode(String name, Function<T, T> nodeFunction) {
            graph.addNode(name, nodeFunction);
            return this;
        }
        
        public StateGraphBuilder<T> setEntryPoint(String nodeName) {
            graph.setEntryPoint(nodeName);
            return this;
        }
        
        public StateGraphBuilder<T> addEdge(String from, String to) {
            graph.addEdge(from, to);
            return this;
        }
        
        public StateGraphBuilder<T> addConditionalEdges(String from, Function<T, String> condition, Map<String, String> routes) {
            graph.addConditionalEdges(from, condition, routes);
            return this;
        }
        
        public StateGraph<T> build() {
            return graph;
        }
    }
}
