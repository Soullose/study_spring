package com.wsf.infrastructure.jpa;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CurrentUserAuditorAware implements AuditorAware<String> {
  @NotNull
  @Override
  public Optional<String> getCurrentAuditor() {
    Optional.ofNullable(SecurityContextHolder.getContext())
        .ifPresent(context -> log.debug("SecurityContext:{}", context));
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    Object principal = authentication.getPrincipal();
    log.debug("UserAccountDetail:-{}", principal);
    // Replace "getUsername()" with the appropriate method to get the current user's identifier
    return Optional.of(authentication.getName());
  }
}
