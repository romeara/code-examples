package com.rsomeara.spring.security.lockout.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;

public class TestLoginAttemptService implements ILoginAttemptService {

    private final Map<String, Integer> loginAttempts;

    public TestLoginAttemptService() {
        loginAttempts = new HashMap<>();
    }

    @Override
    public Integer getFailedAttemptCount(Authentication authentication) {
        return getLogin(authentication)
                .map(loginAttempts::get)
                .orElse(0);
    }

    @Override
    public void incrementFailedAttempts(Authentication authentication) {
        Optional<String> login = getLogin(authentication);

        Integer logins = login
                .map(loginAttempts::get)
                .orElse(0) + 1;

        login.ifPresent(input -> loginAttempts.put(input, logins));
    }

    @Override
    public void clearFailedAttempts(Authentication authentication) {
        getLogin(authentication)
        .ifPresent(loginAttempts::remove);
    }

    private Optional<String> getLogin(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .map(Authentication::getName);
    }

}
