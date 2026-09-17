package com.org.paymentprocessor.security;

import com.org.paymentprocessor.dto.auth.AuthResponse;
import com.org.paymentprocessor.dto.auth.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldLoginWithValidCredentials() {

        LoginRequest request = LoginRequest.builder()
                .email("admin@payments.com")
                .password("admin123")
                .build();

        ResponseEntity<AuthResponse> response =
                restTemplate.postForEntity(
                        "/auth/login",
                        request,
                        AuthResponse.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getToken())
                .isNotBlank();
    }

    @Test
    void shouldRejectPaymentsWithoutToken() {

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/payments",
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAllowPaymentsWithValidToken() {

        String token = loginAndGetToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request =
                new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/payments",
                        HttpMethod.GET,
                        request,
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    private String loginAndGetToken() {

        LoginRequest request = LoginRequest.builder()
                .email("admin@payments.com")
                .password("admin123")
                .build();

        ResponseEntity<AuthResponse> response =
                restTemplate.postForEntity(
                        "/auth/login",
                        request,
                        AuthResponse.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull();

        return response.getBody().getToken();
    }
}