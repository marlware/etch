package com.etch.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "unit-test-secret-key-that-is-at-least-32-bytes";

    @Test
    void roundTripPreservesSubjectAndCustomClaims() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);

        String token = provider.generateToken("demo", Map.of("role", "tester"));
        Claims claims = provider.validateAndGetClaims(token);

        assertThat(claims.getSubject()).isEqualTo("demo");
        assertThat(claims.get("role", String.class)).isEqualTo("tester");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtTokenProvider("too-short", 60_000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void rejectsNullSecret() {
        assertThatThrownBy(() -> new JwtTokenProvider(null, 60_000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        JwtTokenProvider issuer = new JwtTokenProvider(SECRET, 60_000);
        JwtTokenProvider verifier = new JwtTokenProvider("another-secret-key-that-is-also-32-bytes-long", 60_000);

        String token = issuer.generateToken("demo", Map.of());

        assertThatThrownBy(() -> verifier.validateAndGetClaims(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, -1_000);

        String token = provider.generateToken("demo", Map.of());

        assertThatThrownBy(() -> provider.validateAndGetClaims(token)).isInstanceOf(JwtException.class);
    }
}
