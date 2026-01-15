package com.financial.agents.framework.mcp;

import com.financial.agents.framework.tool.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for MCP tools that can be used by LLMs
 * Integrates validation tools with MCP protocol
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolRegistry {
    
    private final CustomerValidationTool customerTool;
    private final CardValidationTool cardTool;
    private final AccountValidationTool accountTool;
    private final OptionalValidationTools optionalTools;
    
    /**
     * Get all MCP tools for validation
     */
    public List<McpTool> getValidationTools() {
        List<McpTool> tools = new ArrayList<>();
        
        // Customer validation tool
        Map<String, Object> customerSchema = new HashMap<>();
        customerSchema.put("type", "object");
        Map<String, Object> customerProps = new HashMap<>();
        customerProps.put("customerId", Map.of("type", "string", "description", "Customer ID to validate"));
        customerSchema.put("properties", customerProps);
        customerSchema.put("required", List.of("customerId"));
        
        tools.add(McpTool.builder()
            .name("validate_customer")
            .description("Validates customer information and checks for risk markers")
            .inputSchema(customerSchema)
            .build());
        
        // Account validation tool
        Map<String, Object> accountSchema = new HashMap<>();
        accountSchema.put("type", "object");
        Map<String, Object> accountProps = new HashMap<>();
        accountProps.put("accountNumber", Map.of("type", "string", "description", "Account number to validate"));
        accountProps.put("customerId", Map.of("type", "string", "description", "Customer ID"));
        accountSchema.put("properties", accountProps);
        accountSchema.put("required", List.of("accountNumber", "customerId"));
        
        tools.add(McpTool.builder()
            .name("validate_account")
            .description("Validates account details, repayment history, and account validity")
            .inputSchema(accountSchema)
            .build());
        
        // Card validation tool
        Map<String, Object> cardSchema = new HashMap<>();
        cardSchema.put("type", "object");
        Map<String, Object> cardProps = new HashMap<>();
        cardProps.put("accountNumber", Map.of("type", "string", "description", "Account number"));
        cardProps.put("cardNumber", Map.of("type", "string", "description", "Card number to validate"));
        cardSchema.put("properties", cardProps);
        cardSchema.put("required", List.of("accountNumber", "cardNumber"));
        
        tools.add(McpTool.builder()
            .name("validate_card")
            .description("Validates card details and checks for fraud indicators")
            .inputSchema(cardSchema)
            .build());
        
        // Optional tools
        Map<String, Object> transactionSchema = new HashMap<>();
        transactionSchema.put("type", "object");
        Map<String, Object> transactionProps = new HashMap<>();
        transactionProps.put("customerId", Map.of("type", "string", "description", "Customer ID"));
        transactionProps.put("months", Map.of("type", "integer", "description", "Number of months", "default", 6));
        transactionSchema.put("properties", transactionProps);
        transactionSchema.put("required", List.of("customerId"));
        
        tools.add(McpTool.builder()
            .name("get_customer_transaction_history")
            .description("Get detailed customer transaction history for the last N months")
            .inputSchema(transactionSchema)
            .build());
        
        Map<String, Object> refundSchema = new HashMap<>();
        refundSchema.put("type", "object");
        Map<String, Object> refundProps = new HashMap<>();
        refundProps.put("accountNumber", Map.of("type", "string", "description", "Account number"));
        refundProps.put("days", Map.of("type", "integer", "description", "Number of days to look back", "default", 30));
        refundSchema.put("properties", refundProps);
        refundSchema.put("required", List.of("accountNumber"));
        
        tools.add(McpTool.builder()
            .name("check_similar_refunds")
            .description("Check for similar refund requests in the system")
            .inputSchema(refundSchema)
            .build());
        
        Map<String, Object> activitySchema = new HashMap<>();
        activitySchema.put("type", "object");
        Map<String, Object> activityProps = new HashMap<>();
        activityProps.put("accountNumber", Map.of("type", "string", "description", "Account number"));
        activityProps.put("days", Map.of("type", "integer", "description", "Number of days", "default", 30));
        activitySchema.put("properties", activityProps);
        activitySchema.put("required", List.of("accountNumber"));
        
        tools.add(McpTool.builder()
            .name("get_account_activity_summary")
            .description("Get detailed account activity summary including recent payments and charges")
            .inputSchema(activitySchema)
            .build());
        
        return tools;
    }
    
    /**
     * Execute an MCP tool by name
     */
    public Object executeTool(String toolName, Map<String, Object> arguments) {
        log.info("Executing MCP tool: {} with arguments: {}", toolName, arguments);
        
        try {
            switch (toolName) {
                case "validate_customer":
                    String customerId = (String) arguments.get("customerId");
                    return customerTool.validateCustomer(customerId);
                    
                case "validate_account":
                    String accountNumber = (String) arguments.get("accountNumber");
                    String custId = (String) arguments.get("customerId");
                    return accountTool.validateAccount(accountNumber, custId);
                    
                case "validate_card":
                    String accNumber = (String) arguments.get("accountNumber");
                    String cardNumber = (String) arguments.get("cardNumber");
                    return cardTool.validateCard(accNumber, cardNumber);
                    
                case "get_customer_transaction_history":
                    String cId = (String) arguments.get("customerId");
                    Integer months = arguments.containsKey("months") ? 
                        ((Number) arguments.get("months")).intValue() : 6;
                    return optionalTools.getCustomerTransactionHistory(cId, months);
                    
                case "check_similar_refunds":
                    String accNum = (String) arguments.get("accountNumber");
                    Integer days = arguments.containsKey("days") ? 
                        ((Number) arguments.get("days")).intValue() : 30;
                    return optionalTools.checkSimilarRefunds(accNum, days);
                    
                case "get_account_activity_summary":
                    String accountNum = (String) arguments.get("accountNumber");
                    Integer dayCount = arguments.containsKey("days") ? 
                        ((Number) arguments.get("days")).intValue() : 30;
                    return optionalTools.getAccountActivitySummary(accountNum, dayCount);
                    
                default:
                    log.warn("Unknown MCP tool: {}", toolName);
                    return Map.of("error", "Unknown tool: " + toolName);
            }
        } catch (Exception e) {
            log.error("Error executing MCP tool: {}", toolName, e);
            return Map.of("error", "Tool execution failed: " + e.getMessage());
        }
    }
}
