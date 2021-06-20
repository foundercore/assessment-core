package com.assessment.iam.configurations;

import org.springframework.data.domain.AuditorAware;

import com.assessment.iam.commons.AuthUtils;

import java.util.Optional;

class SpringSecurityAuditorAware implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    return Optional.of( AuthUtils.getCurrentQualifiedUsername());
  }
}