package com.financial.agents.framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CreditRefundAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditRefundAgentApplication.class, args);
    }
}
