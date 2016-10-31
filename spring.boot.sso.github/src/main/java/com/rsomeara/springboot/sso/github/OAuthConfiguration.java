package com.rsomeara.springboot.sso.github;

import java.util.Arrays;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;

@Configuration
public class OAuthConfiguration {

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean(name = "gitHubClient")
    public AuthorizationCodeResourceDetails gitHubClient() {
        AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

        client.setClientId("eb035a8504993840473b");
        client.setClientSecret("13777a3a0891f06708332abddd6dd2277cfcdb14");
        client.setAccessTokenUri("https://github.com/login/oauth/access_token");
        client.setUserAuthorizationUri("https://github.com/login/oauth/authorize");
        client.setClientAuthenticationScheme(AuthenticationScheme.form);

        client.setScope(Arrays.asList("public_repo"));

        return client;
    }

    @Bean(name = "gitHubResource")
    public ResourceServerProperties gitHubResource() {
        ResourceServerProperties resource = new ResourceServerProperties();

        resource.setUserInfoUri("https://api.github.com/user");

        return resource;
    }

    @Bean(name = "gitHubRestTemplate")
    public OAuth2RestTemplate gitHubRestTemplate(@Qualifier("gitHubClient") AuthorizationCodeResourceDetails gitHubClient,
            OAuth2ClientContext oauth2ClientContext) {
        return new OAuth2RestTemplate(gitHubClient, oauth2ClientContext);
    }

    @Bean(name = "gitHubFilter")
    public Filter gitHubFilter(@Qualifier("gitHubClient") AuthorizationCodeResourceDetails client,
            @Qualifier("gitHubResource") ResourceServerProperties resource,
            @Qualifier("gitHubRestTemplate") OAuth2RestTemplate gitHubRestTemplate) {
        OAuth2ClientAuthenticationProcessingFilter oAuth2ClientAuthenticationFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/github");
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(resource.getUserInfoUri(), client.getClientId());

        tokenServices.setRestTemplate(gitHubRestTemplate);

        oAuth2ClientAuthenticationFilter.setRestTemplate(gitHubRestTemplate);
        oAuth2ClientAuthenticationFilter.setTokenServices(tokenServices);

        return oAuth2ClientAuthenticationFilter;
    }

}
