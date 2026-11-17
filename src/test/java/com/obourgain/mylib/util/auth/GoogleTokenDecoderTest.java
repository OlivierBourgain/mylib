package com.obourgain.mylib.util.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class GoogleTokenDecoderTest {
    @Test
    void verifiesSignatureIssuerExpiryAndAudience() throws Exception {
        RSAKey key = new RSAKeyGenerator(2048).generate();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey()).build();
        decoder.setJwtValidator(ApiSecurity.googleTokenValidator("client"));
        Instant future = Instant.now().plusSeconds(3600);
        assertEquals("user", decoder.decode(sign(key, "https://accounts.google.com", "client", future)).getSubject());
        assertEquals("user", decoder.decode(sign(key, "accounts.google.com", "client", future)).getSubject());
        assertThrows(JwtException.class, () -> decoder.decode(sign(key, "https://attacker.example", "client", future)));
        assertThrows(JwtException.class, () -> decoder.decode(sign(key, "https://accounts.google.com", "other", future)));
        assertThrows(JwtException.class, () -> decoder.decode(sign(key, "https://accounts.google.com", "client", Instant.now().minusSeconds(3600))));
        RSAKey otherKey = new RSAKeyGenerator(2048).generate();
        assertThrows(JwtException.class, () -> decoder.decode(sign(otherKey, "https://accounts.google.com", "client", future)));
    }

    private String sign(RSAKey key, String issuer, String audience, Instant expiry) throws JOSEException {
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), new JWTClaimsSet.Builder()
                .subject("user").issuer(issuer).audience(audience).expirationTime(Date.from(expiry)).build());
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }
}
