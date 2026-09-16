package com.org.paymentprocessor.dto.api;

import com.org.paymentprocessor.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String id;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private OffsetDateTime timestamp;
    private PaymentStatus status;
    private String message;
    private OffsetDateTime processedAt;
}
