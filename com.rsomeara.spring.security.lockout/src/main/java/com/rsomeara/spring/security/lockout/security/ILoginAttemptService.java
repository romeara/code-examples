package com.rsomeara.spring.security.lockout.security;

import org.springframework.security.core.Authentication;

public interface ILoginAttemptService {

    Integer getFailedAttemptCount(Authentication authentication);

    void incrementFailedAttempts(Authentication authentication);

    void clearFailedAttempts(Authentication authentication);

}
