package com.org.paymentprocessor.dto.api;

import com.org.paymentprocessor.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private PaymentStatus status;
    private String message;
    private OffsetDateTime processedAt;
}
