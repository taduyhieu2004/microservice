package com.taskflow.common.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String SYSTEM = "SYSTEM";
    private static final String ANONYMOUS = "anonymousUser";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of(SYSTEM);
        }
        Object principal = authentication.getPrincipal();
        if (principal == null || ANONYMOUS.equals(principal)) {
            return Optional.of(SYSTEM);
        }
        return Optional.of(authentication.getName());
    }
}
