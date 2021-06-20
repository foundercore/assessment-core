package com.assessment.iam.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
class AuditConfig {

  @Bean
  public AuditorAware<String> auditorProvider() {
    return new SpringSecurityAuditorAware();
  }
}