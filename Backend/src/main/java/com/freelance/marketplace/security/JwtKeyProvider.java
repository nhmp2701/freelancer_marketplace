package com.freelance.marketplace.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class JwtKeyProvider {

  private static final int MINIMUM_SECRET_BYTES = 32;
  private final SecretKey signingKey;

  public JwtKeyProvider(@Value("${jwt.secret:}") String secret, Environment environment) {
    if (secret == null || secret.isBlank()) {
      if (environment.acceptsProfiles(Profiles.of("prod"))) {
        throw new IllegalStateException("JWT_SECRET is required when the prod profile is active");
      }
      this.signingKey = Jwts.SIG.HS256.key().build();
      return;
    }

    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < MINIMUM_SECRET_BYTES) {
      throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes");
    }
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
  }

  public SecretKey getSigningKey() {
    return signingKey;
  }
}
