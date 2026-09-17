package com.org.paymentprocessor.controller;

import com.org.paymentprocessor.dto.api.PaymentResponse;
import com.org.paymentprocessor.exception.ApiError;
import com.org.paymentprocessor.exception.InvalidDateRangeException;
import com.org.paymentprocessor.model.PaymentStatus;
import com.org.paymentprocessor.model.User;
import com.org.paymentprocessor.repository.UserRepository;
import com.org.paymentprocessor.security.JwtService;
import com.org.paymentprocessor.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureTestRestTemplate
class PaymentGetTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void shouldReturnAllPaymentsWhenFiltersAreMissing() {

        when(
                paymentService.getPayments(
                        null,
                        null,
                        null
                )
        ).thenReturn(
                List.of(paymentResponse())
        );

        ResponseEntity<PaymentResponse[]> response =
                restTemplate.exchange(
                        "/payments",
                        HttpMethod.GET,
                        new HttpEntity<>(
                                authenticatedHeaders()
                        ),
                        PaymentResponse[].class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull()
                .hasSize(1);

        PaymentResponse payment =
                response.getBody()[0];

        assertThat(payment.getId())
                .isEqualTo("PAY-1001");

        assertThat(payment.getCustomerId())
                .isEqualTo("CUST-10");

        assertThat(payment.getAmount())
                .isEqualByComparingTo("125.50");

        assertThat(payment.getCurrency())
                .isEqualTo("USD");

        assertThat(payment.getStatus())
                .isEqualTo(PaymentStatus.PROCESSED);

        verify(paymentService)
                .getPayments(
                        null,
                        null,
                        null
                );
    }

    @Test
    void shouldPassFiltersToPaymentService() {

        OffsetDateTime from =
                OffsetDateTime.parse(
                        "2026-09-01T00:00:00Z"
                );

        OffsetDateTime to =
                OffsetDateTime.parse(
                        "2026-09-30T23:59:59Z"
                );

        when(
                paymentService.getPayments(
                        "CUST-10",
                        from,
                        to
                )
        ).thenReturn(
                List.of(paymentResponse())
        );

        String url =
                "/payments"
                        + "?customerId=CUST-10"
                        + "&from=2026-09-01T00:00:00Z"
                        + "&to=2026-09-30T23:59:59Z";

        ResponseEntity<PaymentResponse[]> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(
                                authenticatedHeaders()
                        ),
                        PaymentResponse[].class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull()
                .hasSize(1);

        verify(paymentService)
                .getPayments(
                        "CUST-10",
                        from,
                        to
                );
    }

    @Test
    void shouldReturnBadRequestForInvalidDateRange() {

        OffsetDateTime from =
                OffsetDateTime.parse(
                        "2026-09-30T00:00:00Z"
                );

        OffsetDateTime to =
                OffsetDateTime.parse(
                        "2026-09-01T00:00:00Z"
                );

        when(
                paymentService.getPayments(
                        null,
                        from,
                        to
                )
        ).thenThrow(
                new InvalidDateRangeException(
                        "'from' must be earlier than or equal to 'to'"
                )
        );

        String url =
                "/payments"
                        + "?from=2026-09-30T00:00:00Z"
                        + "&to=2026-09-01T00:00:00Z";

        ResponseEntity<ApiError> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(
                                authenticatedHeaders()
                        ),
                        ApiError.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getStatus())
                .isEqualTo(400);

        assertThat(response.getBody().getError())
                .isEqualTo("Invalid date range");

        assertThat(response.getBody().getMessage())
                .isEqualTo(
                        "'from' must be earlier than or equal to 'to'"
                );

        assertThat(response.getBody().getPath())
                .isEqualTo("/payments");
    }

    private PaymentResponse paymentResponse() {

        return PaymentResponse.builder()
                .id("PAY-1001")
                .customerId("CUST-10")
                .amount(
                        new BigDecimal("125.50")
                )
                .currency("USD")
                .timestamp(
                        OffsetDateTime.parse(
                                "2026-09-16T10:00:00-06:00"
                        )
                )
                .status(
                        PaymentStatus.PROCESSED
                )
                .message(
                        "Payment processed successfully"
                )
                .processedAt(
                        OffsetDateTime.parse(
                                "2026-09-16T10:00:01-06:00"
                        )
                )
                .build();
    }

    private HttpHeaders authenticatedHeaders() {

        User user = userRepository
                .findByEmail("admin@payments.com")
                .orElseThrow();

        String token =
                jwtService.generateToken(user);

        HttpHeaders headers =
                new HttpHeaders();

        headers.setBearerAuth(token);

        return headers;
    }
}