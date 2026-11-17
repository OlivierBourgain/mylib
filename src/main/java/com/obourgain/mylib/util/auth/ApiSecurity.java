package com.obourgain.mylib.util.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class ApiSecurity {
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Only Authorization-header bearer tokens authenticate this chain; cookies are ignored.
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(resource -> resource.jwt(withDefaults()))
                .build();
    }

    @Bean
    public JwtDecoder googleIdTokenDecoder(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
        decoder.setJwtValidator(googleTokenValidator(clientId));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> googleTokenValidator(String clientId) {
        return new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                googleClaimsValidator(clientId));
    }

    static OAuth2TokenValidator<Jwt> googleClaimsValidator(String clientId) {
        return jwt -> {
            String subject = jwt.getSubject();
            String authorizedParty = jwt.getClaimAsString("azp");
            String issuer = jwt.getClaimAsString("iss");
            if (("https://accounts.google.com".equals(issuer) || "accounts.google.com".equals(issuer))
                    && jwt.getAudience() != null && jwt.getAudience().contains(clientId)
                    && (authorizedParty == null || clientId.equals(authorizedParty))
                    && subject != null && !subject.isBlank() && jwt.getExpiresAt() != null) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid Google ID token claims", null));
        };
    }
}
