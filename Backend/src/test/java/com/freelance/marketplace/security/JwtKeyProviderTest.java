package com.freelance.marketplace.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtKeyProviderTest {

    @Test
    void generatesEphemeralKeyOutsideProductionWhenSecretIsMissing() {
        JwtKeyProvider provider = new JwtKeyProvider("", new MockEnvironment());

        assertThat(provider.getSigningKey().getEncoded()).hasSize(32);
    }

    @Test
    void requiresConfiguredSecretInProduction() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        assertThatThrownBy(() -> new JwtKeyProvider("", environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET is required");
    }

    @Test
    void rejectsWeakConfiguredSecret() {
        assertThatThrownBy(() -> new JwtKeyProvider("too-short", new MockEnvironment()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32");
    }

    @Test
    void generatedKeySignsAndValidatesTokens() {
        JwtUtil jwtUtil = new JwtUtil(new JwtKeyProvider("", new MockEnvironment()), 60_000);

        String token = jwtUtil.generateToken("user@example.com", "USER");

        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("USER");
        assertThat(jwtUtil.validateToken(token, "user@example.com")).isTrue();
    }
}
