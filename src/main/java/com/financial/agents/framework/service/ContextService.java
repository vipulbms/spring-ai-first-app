package com.financial.agents.framework.service;

import com.financial.agents.framework.domain.RefundRequest;
import com.financial.agents.framework.tool.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing context and validation data across the refund process
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContextService {
    
    private final CustomerValidationTool customerTool;
    private final CardValidationTool cardTool;
    private final AccountValidationTool accountTool;
    private final RiskAssessmentTool riskTool;
    
    /**
     * Build comprehensive context for a refund request
     * Performs all validations and risk assessments
     */
    public Map<String, Object> buildContext(RefundRequest request) {
        log.info("Building context for request: {}", request.getRequestId());
        
        Map<String, Object> context = new HashMap<>();
        
        // Validate customer
        CustomerValidationTool.CustomerValidationResult customerValidation = 
            customerTool.validateCustomer(request.getCustomerId());
        context.put("customerValidation", customerValidation);
        context.put("customerDetails", customerTool.getCustomerDetails(request.getCustomerId()));
        
        // Validate account
        AccountValidationTool.AccountValidationResult accountValidation = 
            accountTool.validateAccount(request.getAccountNumber(), request.getCustomerId());
        context.put("accountValidation", accountValidation);
        context.put("accountDetails", accountTool.getAccountDetails(request.getAccountNumber()));
        context.put("repaymentHistory", accountValidation.getRepaymentHistory());
        
        // Validate card (if applicable)
        CardValidationTool.CardValidationResult cardValidation = 
            cardTool.validateCard(request.getAccountNumber(), request.getAccountNumber());
        context.put("cardValidation", cardValidation);
        context.put("cardDetails", cardTool.getCardDetails(request.getAccountNumber()));
        
        // Perform comprehensive risk assessment
        RiskAssessmentTool.RiskAssessmentResult riskAssessment = 
            riskTool.assessRisk(request, context);
        context.put("riskAssessment", riskAssessment);
        
        // Add summary
        context.put("validationSummary", buildValidationSummary(context));
        
        log.debug("Context built with {} entries", context.size());
        return context;
    }
    
    /**
     * Build a human-readable validation summary
     */
    private Map<String, Object> buildValidationSummary(Map<String, Object> context) {
        Map<String, Object> summary = new HashMap<>();
        
        if (context.containsKey("customerValidation")) {
            CustomerValidationTool.CustomerValidationResult customer = 
                (CustomerValidationTool.CustomerValidationResult) context.get("customerValidation");
            summary.put("customerValid", customer.isValid());
            summary.put("customerActive", customer.isActive());
            summary.put("customerRiskScore", customer.getRiskScore());
        }
        
        if (context.containsKey("accountValidation")) {
            AccountValidationTool.AccountValidationResult account = 
                (AccountValidationTool.AccountValidationResult) context.get("accountValidation");
            summary.put("accountValid", account.isValid());
            summary.put("accountActive", account.isActive());
            summary.put("paymentHistoryScore", account.getPaymentHistoryScore());
            summary.put("overdueAmount", account.getOverdueAmount());
        }
        
        if (context.containsKey("riskAssessment")) {
            RiskAssessmentTool.RiskAssessmentResult risk = 
                (RiskAssessmentTool.RiskAssessmentResult) context.get("riskAssessment");
            summary.put("riskScore", risk.getRiskScore());
            summary.put("riskLevel", risk.getRiskLevel());
            summary.put("recommendation", risk.getRecommendation());
        }
        
        return summary;
    }
    
    /**
     * Get validation data from context
     */
    public Map<String, Object> getValidationData(Map<String, Object> context) {
        Map<String, Object> validationData = new HashMap<>();
        
        if (context.containsKey("customerValidation")) {
            validationData.put("customerValidation", context.get("customerValidation"));
        }
        if (context.containsKey("accountValidation")) {
            validationData.put("accountValidation", context.get("accountValidation"));
        }
        if (context.containsKey("cardValidation")) {
            validationData.put("cardValidation", context.get("cardValidation"));
        }
        if (context.containsKey("riskAssessment")) {
            validationData.put("riskAssessment", context.get("riskAssessment"));
        }
        
        return validationData;
    }
}
