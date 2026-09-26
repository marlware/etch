package com.etch.apigateway.web;

import com.etch.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest {

    private final JwtTokenProvider provider =
            new JwtTokenProvider("test-only-etch-gateway-secret-needs-32-bytes-min", 60_000);
    private final AuthController controller = new AuthController(provider, 60_000);

    @Test
    void issuesABearerTokenForTheRequestedUser() {
        ResponseEntity<AuthResponse> response = controller.issueToken(new AuthRequest("demo"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse body = response.getBody();
        assertThat(body.tokenType()).isEqualTo("Bearer");
        assertThat(body.expiresInMs()).isEqualTo(60_000);
        assertThat(provider.validateAndGetClaims(body.accessToken()).getSubject()).isEqualTo("demo");
    }
}
