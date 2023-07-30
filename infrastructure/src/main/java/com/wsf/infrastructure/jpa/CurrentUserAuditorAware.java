package com.wsf.infrastructure.jpa;

import com.wsf.infrastructure.security.domain.UserAccountDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Slf4j
public class CurrentUserAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        log.debug("UserAccountDetail:-{}",principal);
        // Replace "getUsername()" with the appropriate method to get the current user's identifier
        return Optional.of(authentication.getName());
    }
}
