package com.assessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AssessmentCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssessmentCoreApplication.class, args);
    }
}
