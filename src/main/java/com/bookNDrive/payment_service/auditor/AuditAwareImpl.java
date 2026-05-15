package com.bookNDrive.payment_service.auditor;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditAwareImpl")
public class AuditAwareImpl implements AuditorAware<String> {

    private static final String ANONYMOUS_USER = "anonymousUser";
    private static final String SYSTEM_USER = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of(SYSTEM_USER);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            return hasText(username) && !ANONYMOUS_USER.equals(username)
                    ? Optional.of(username)
                    : Optional.of(SYSTEM_USER);
        }

        String name = authentication.getName();
        return hasText(name) && !ANONYMOUS_USER.equals(name)
                ? Optional.of(name)
                : Optional.of(SYSTEM_USER);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
