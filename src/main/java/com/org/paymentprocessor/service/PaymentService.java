package com.org.paymentprocessor.service;

import com.org.paymentprocessor.dto.api.PaymentRequest;
import com.org.paymentprocessor.dto.api.PaymentResponse;
import com.org.paymentprocessor.exception.InvalidDateRangeException;
import com.org.paymentprocessor.model.Payment;
import com.org.paymentprocessor.model.PaymentStatus;
import com.org.paymentprocessor.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    private static final OffsetDateTime DEFAULT_FROM =
            OffsetDateTime.parse("0001-01-01T00:00:00Z");

    private static final OffsetDateTime DEFAULT_TO =
            OffsetDateTime.parse("9999-12-31T23:59:59Z");

    public List<PaymentResponse> getPayments(
            String customerId,
            OffsetDateTime from,
            OffsetDateTime to
    ) throws InvalidDateRangeException {
        OffsetDateTime effectiveFrom =
                from != null ? from : DEFAULT_FROM;

        OffsetDateTime effectiveTo =
                to != null ? to : DEFAULT_TO;

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new InvalidDateRangeException(
                    "'from' must be earlier than or equal to 'to'"
            );
        }

        List<Payment> payments;

        if (customerId == null || customerId.isBlank()) {
            payments = paymentRepository.findByTimestampBetween(
                    effectiveFrom,
                    effectiveTo
            );
        } else {
            payments =
                    paymentRepository.findByCustomerIdAndTimestampBetween(
                            customerId,
                            effectiveFrom,
                            effectiveTo
                    );
        }

        return payments.stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .timestamp(payment.getTimestamp())
                .status(payment.getStatus())
                .message(responseMessage(payment.getStatus()))
                .processedAt(payment.getProcessedAt())
                .build();
    }

    private String responseMessage(PaymentStatus status) {
        return switch (status) {
            case PROCESSED -> "Payment processed successfully";
            case FAILED -> "Payment processing failed";
        };
    }

}
