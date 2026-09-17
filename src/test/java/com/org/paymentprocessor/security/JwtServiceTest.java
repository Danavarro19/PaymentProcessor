package com.org.paymentprocessor.security;

import com.org.paymentprocessor.model.Role;
import com.org.paymentprocessor.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRLZXlGb3JIUzI1Ng=="
        );

        ReflectionTestUtils.setField(
                jwtService,
                "expiration",
                3600000L
        );

        user = User.builder()
                .email("admin@payments.com")
                .password("password")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    void shouldGenerateTokenAndExtractUsername() {
        String token = jwtService.generateToken(user);

        String username = jwtService.extractUsername(token);

        assertThat(token).isNotBlank();
        assertThat(username).isEqualTo("admin@payments.com");
    }

    @Test
    void shouldValidateTokenForCorrectUser() {
        String token = jwtService.generateToken(user);

        boolean valid = jwtService.isTokenValid(token, user);

        assertThat(valid).isTrue();
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        String token = jwtService.generateToken(user);

        User differentUser = User.builder()
                .email("other@payments.com")
                .password("password")
                .role(Role.USER)
                .build();

        boolean valid =
                jwtService.isTokenValid(token, differentUser);

        assertThat(valid).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        ReflectionTestUtils.setField(
                jwtService,
                "expiration",
                -1000L
        );

        String token = jwtService.generateToken(user);

        boolean valid = jwtService.isTokenValid(token, user);

        assertThat(valid).isFalse();
    }
}