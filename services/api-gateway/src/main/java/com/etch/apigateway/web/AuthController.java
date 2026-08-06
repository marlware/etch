package com.etch.apigateway.web;

import com.etch.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Issues demo bearer tokens so the routing/rate-limiting/auth-filter
 * pipeline can be exercised end to end. Etch has no real identity
 * provider (README lists authentication as "optional if added") -- this
 * is intentionally just enough to prove the gateway enforces it, not a
 * production login flow.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final long expirationMs;

    public AuthController(JwtTokenProvider jwtTokenProvider,
                           @Value("${etch.jwt.expiration-ms:3600000}") long expirationMs) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.expirationMs = expirationMs;
    }

    @PostMapping("/token")
    public ResponseEntity<AuthResponse> issueToken(@Valid @RequestBody AuthRequest request) {
        String token = jwtTokenProvider.generateToken(request.username(), Map.of());
        return ResponseEntity.ok(AuthResponse.bearer(token, expirationMs));
    }
}
