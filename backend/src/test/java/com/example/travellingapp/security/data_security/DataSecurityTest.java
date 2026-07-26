package com.example.travellingapp.security.data_security;

import com.example.travellingapp.enums.OtpPurpose;
import com.example.travellingapp.security.TokenSecretProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataSecurityTest {
    private DataSecurity dataSecurity;

    @BeforeEach
    void setUp() {
        TokenSecretProvider tokenSecretProvider = mock(TokenSecretProvider.class);
        when(tokenSecretProvider.getRefreshTokenHashKey()).thenReturn(
                new SecretKeySpec(
                        "test-refresh-token-hash-secret-at-least-thirty-two-bytes"
                                .getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"
                )
        );
        when(tokenSecretProvider.getOtpHashKey()).thenReturn(
                new SecretKeySpec(
                        "test-otp-hash-secret-at-least-thirty-two-bytes"
                                .getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"
                )
        );
        dataSecurity = new DataSecurity(tokenSecretProvider);
    }

    @Test
    void hashData_shouldCreateDeterministicHmacUsingDedicatedRefreshSecret() {
        String firstHash = dataSecurity.hashData("refresh-token-value");
        String secondHash = dataSecurity.hashData("refresh-token-value");

        assertThat(firstHash).isEqualTo(secondHash);
        assertThat(firstHash).isNotEqualTo("refresh-token-value");
    }

    @Test
    void hashData_shouldProduceDifferentHashesForDifferentTokens() {
        assertThat(dataSecurity.hashData("refresh-token-one"))
                .isNotEqualTo(dataSecurity.hashData("refresh-token-two"));
    }

    @Test
    void hashOtp_shouldCreateNonPlaintextPurposeBoundHash() {
        String hash = dataSecurity.hashOtp("JustinBo123", OtpPurpose.REGISTRATION, "123456");

        assertThat(hash).isNotEqualTo("123456");
        assertThat(hash).hasSize(44);
    }

    @Test
    void matchesOtp_shouldReturnTrueOnlyForMatchingUsernamePurposeAndCode() {
        String hash = dataSecurity.hashOtp("JustinBo123", OtpPurpose.REGISTRATION, "123456");

        assertThat(dataSecurity.matchesOtp("JustinBo123", OtpPurpose.REGISTRATION, "123456", hash)).isTrue();
        assertThat(dataSecurity.matchesOtp("AnotherUser", OtpPurpose.REGISTRATION, "123456", hash)).isFalse();
        assertThat(dataSecurity.matchesOtp("JustinBo123", OtpPurpose.PASSWORD_RESET, "123456", hash)).isFalse();
        assertThat(dataSecurity.matchesOtp("JustinBo123", OtpPurpose.REGISTRATION, "654321", hash)).isFalse();
    }
}
