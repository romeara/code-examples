package com.rsomeara.spring.security.lockout.security;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class LoginLockoutSecurityConfigurerAdapter
extends SecurityConfigurerAdapter<AuthenticationManager, AuthenticationManagerBuilder> {

    private static final int DEFAULT_MAXIMUM_ATTEMPTS = 3;

    private ILoginAttemptService loginAttemptService;

    private int maximumAttempts;

    public LoginLockoutSecurityConfigurerAdapter() {
        maximumAttempts = DEFAULT_MAXIMUM_ATTEMPTS;
        loginAttemptService = new TestLoginAttemptService();
    }

    @Override
    public void configure(AuthenticationManagerBuilder builder) {
        builder.authenticationProvider(new LoginLimitAuthenticationProvider(loginAttemptService, maximumAttempts));
    }

    public LoginLockoutSecurityConfigurerAdapter maximumAttempts(int maximumAttempts) {
        if (maximumAttempts <= 0) {
            throw new IllegalArgumentException("Cannot allow less than 1 login attempt");
        }

        this.maximumAttempts = maximumAttempts;

        return this;
    }

    public LoginLockoutSecurityConfigurerAdapter loginAttemptService(ILoginAttemptService loginAttemptService) {
        Objects.requireNonNull(loginAttemptService);

        this.loginAttemptService = loginAttemptService;

        return this;
    }

    @EventListener
    public void authenticationSucceeded(AuthenticationSuccessEvent event) {
        loginAttemptService.clearFailedAttempts(event.getAuthentication());
    }

    @EventListener
    public void authenticationFailed(AuthenticationFailureBadCredentialsEvent event) {
        loginAttemptService.incrementFailedAttempts(event.getAuthentication());
    }

    private static final class LoginLimitAuthenticationProvider implements AuthenticationProvider {

        private final ILoginAttemptService loginAttemptService;

        private final int maximumAttempts;

        protected LoginLimitAuthenticationProvider(ILoginAttemptService loginAttemptService, int maximumAttempts) {
            this.loginAttemptService = Objects.requireNonNull(loginAttemptService);
            this.maximumAttempts = maximumAttempts;
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            if (loginAttemptService.getFailedAttemptCount(authentication) >= maximumAttempts) {
                throw new LockedException("Too many failed login attempts, try again later");
            }

            // We never return the authentication, as we aren't actually applying it, just checking it
            return null;
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return Authentication.class.isAssignableFrom(authentication);
        }

    }

}
