package com.financial.agents.framework;

import com.financial.agents.framework.controller.RefundController;
import com.financial.agents.framework.service.RefundOrchestrationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RefundControllerTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate(new RestTemplateBuilder());

    @Test
    public void testHealthEndpoint() {
        String url = "http://localhost:" + port + "/api/refund/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("running"));
    }

    @Test
    public void testProcessRefundRequest() {
        String url = "http://localhost:" + port + "/api/refund/process";
        
        RefundController.RefundRequestDto request = new RefundController.RefundRequestDto();
        request.setCustomerId("CUST123");
        request.setAccountNumber("ACC456789");
        request.setRefundAmount(new BigDecimal("500.00"));
        request.setCurrency("USD");
        request.setReason("Overpayment due to duplicate charge");
        request.setCustomerEmail("customer@example.com");
        request.setCustomerPhone("+1234567890");
        
        ResponseEntity<RefundOrchestrationService.RefundProcessResult> response = 
            restTemplate.postForEntity(url, request, RefundOrchestrationService.RefundProcessResult.class);
        
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getRequestId());
        assertNotNull(response.getBody().getStatus());
    }
}
