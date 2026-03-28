package com.malgn.infrastructure.persistence.auditing;

import com.malgn.infrastructure.security.userdetails.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken)
            return Optional.of("SYSTEM");

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails)
            return Optional.of(((CustomUserDetails) principal).getUsername());

        return Optional.empty();
    }
}
