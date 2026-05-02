package com.eticaret.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AIServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AIServiceApplication.class, args);
    }
}