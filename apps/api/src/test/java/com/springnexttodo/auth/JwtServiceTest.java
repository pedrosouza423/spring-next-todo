package com.springnexttodo.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "Z3VhcmRhZXNzZXNlZ3JlZG9tdWl0b2JlbXBvcmZhdm9yYXF1aQ==";
    private static final long EXPIRATION_MS = 86_400_000L;

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
        user = new User();
        user.setName("Pedro");
        user.setEmail("pedro@example.com");
    }

    @Test
    void generateToken_and_extractEmail_roundtrip() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("pedro@example.com");
    }

    @Test
    void isValid_returns_true_for_valid_token() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_returns_false_for_expired_token() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1L);
        String token = shortLived.generateToken(user);

        Thread.sleep(5);

        assertThat(shortLived.isValid(token)).isFalse();
    }

    @Test
    void isValid_returns_false_for_tampered_token() {
        String token = jwtService.generateToken(user);
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }

    @Test
    void isValid_returns_false_for_null() {
        assertThat(jwtService.isValid(null)).isFalse();
    }

    @Test
    void isValid_returns_false_for_blank() {
        assertThat(jwtService.isValid("")).isFalse();
        assertThat(jwtService.isValid("   ")).isFalse();
    }
}
