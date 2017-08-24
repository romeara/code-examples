package com.rsomeara.spring.security.lockout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.rsomeara.spring.security.lockout.security.LoginLockoutSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private LoginLockoutSecurityConfigurerAdapter loginLockoutSecurityConfigurerAdapter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/login/**", "/j_spring_security_check/**").permitAll()
        .anyRequest().fullyAuthenticated().and()
        .formLogin()
        .loginPage("/login")
        .loginProcessingUrl("/j_spring_security_check");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // The login lockout configuration must be applied before others so that the login limit is checked before
        // the credentials are tried, otherwise the attacker is still able to brute-force attack the credentials
        auth.apply(loginLockoutSecurityConfigurerAdapter);

        // Test user
        auth.inMemoryAuthentication().withUser("user").password("password");
    }

    @Bean
    public LoginLockoutSecurityConfigurerAdapter loginLockoutSecurityConfigurerAdapter() {
        return new LoginLockoutSecurityConfigurerAdapter();
    }

}
