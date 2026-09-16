package com.org.paymentprocessor.controller;

import com.org.paymentprocessor.dto.api.PaymentRequest;
import com.org.paymentprocessor.dto.api.PaymentResponse;
import com.org.paymentprocessor.model.PaymentStatus;
import com.org.paymentprocessor.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class PaymentCreateTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void shouldCreatePayment() {
        OffsetDateTime paymentTimestamp =
                OffsetDateTime.parse("2026-09-16T10:00:00-06:00");

        OffsetDateTime processedAt =
                OffsetDateTime.parse("2026-09-16T10:00:01-06:00");

        PaymentRequest request = PaymentRequest.builder()
                .id("PAY-1001")
                .customerId("CUST-10")
                .amount(new BigDecimal("125.50"))
                .currency("USD")
                .timestamp(paymentTimestamp)
                .build();

        PaymentResponse serviceResponse = PaymentResponse.builder()
                .id("PAY-1001")
                .customerId("CUST-10")
                .amount(new BigDecimal("125.50"))
                .currency("USD")
                .timestamp(paymentTimestamp)
                .status(PaymentStatus.PROCESSED)
                .message("Payment processed successfully")
                .processedAt(processedAt)
                .build();

        when(paymentService.createPayment(any(PaymentRequest.class)))
                .thenReturn(serviceResponse);

        ResponseEntity<PaymentResponse> response =
                restTemplate.postForEntity(
                        "/payments",
                        request,
                        PaymentResponse.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getHeaders().getLocation())
                .isNotNull();

        assertThat(response.getHeaders().getLocation().getPath())
                .isEqualTo("/payments/PAY-1001");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo("PAY-1001");
        assertThat(response.getBody().getCustomerId()).isEqualTo("CUST-10");
        assertThat(response.getBody().getAmount())
                .isEqualByComparingTo("125.50");
        assertThat(response.getBody().getCurrency()).isEqualTo("USD");
        assertThat(response.getBody().getStatus())
                .isEqualTo(PaymentStatus.PROCESSED);
        assertThat(response.getBody().getMessage())
                .isEqualTo("Payment processed successfully");

        verify(paymentService).createPayment(any(PaymentRequest.class));
    }
}