package com.example.travellingapp.security.data_security;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Getter
@Component
public class TokenSecretProvider {

    private static final int HS512_MINIMUM_BYTES = 64;
    private static final int HMAC_SHA_256_MINIMUM_BYTES = 32;

    private final SecretKey jwtSigningKey;
    private final SecretKey refreshTokenHashKey;

    public TokenSecretProvider(
            @Value("${app.security.jwt-secret}") String jwtSecret,
            @Value("${app.security.refresh-token-hash-secret}")
            String refreshTokenHashSecret
    ) {
        byte[] jwtSecretBytes = requireSecret(
                "JWT_SECRET",
                jwtSecret,
                HS512_MINIMUM_BYTES
        );

        byte[] refreshHashSecretBytes = requireSecret(
                "REFRESH_TOKEN_HASH_SECRET",
                refreshTokenHashSecret,
                HMAC_SHA_256_MINIMUM_BYTES
        );

        this.jwtSigningKey = Keys.hmacShaKeyFor(jwtSecretBytes);
        this.refreshTokenHashKey = new SecretKeySpec(
                refreshHashSecretBytes,
                "HmacSHA256"
        );
    }

    private static byte[] requireSecret(
            String environmentName,
            String value,
            int minimumBytes
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    environmentName + " must be configured"
            );
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < minimumBytes) {
            throw new IllegalStateException(
                    environmentName
                            + " must contain at least "
                            + minimumBytes
                            + " UTF-8 bytes"
            );
        }

        return bytes;
    }
}