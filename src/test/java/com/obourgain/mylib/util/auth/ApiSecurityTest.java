package com.obourgain.mylib.util.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiSecurityTest {
    @Test
    void validatesAudienceSubjectExpiryAndAuthorizedParty() {
        var validator = ApiSecurity.googleClaimsValidator("client");
        var valid = token().build();
        assertFalse(validator.validate(valid).hasErrors());
        assertTrue(validator.validate(token().claims(claims -> claims.remove("aud")).build()).hasErrors());
        assertTrue(validator.validate(token().claims(claims -> claims.remove("sub")).build()).hasErrors());
        assertTrue(validator.validate(token().audience(List.of("other-client")).build()).hasErrors());
        assertTrue(validator.validate(token().subject(" ").build()).hasErrors());
        assertTrue(validator.validate(token().claim("azp", "other-client").build()).hasErrors());
        assertFalse(validator.validate(token().claim("azp", "client").build()).hasErrors());
        assertTrue(validator.validate(Jwt.withTokenValue("token").header("alg", "RS256")
                .subject("user").claim("iss", "https://accounts.google.com").audience(List.of("client")).build()).hasErrors());
    }

    private Jwt.Builder token() {
        return Jwt.withTokenValue("token").header("alg", "RS256").subject("user")
                .claim("iss", "https://accounts.google.com").audience(List.of("client")).expiresAt(Instant.now().plusSeconds(3600));
    }
}
