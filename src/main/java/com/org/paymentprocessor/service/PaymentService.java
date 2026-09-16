package com.org.paymentprocessor.service;

import com.org.paymentprocessor.dto.api.PaymentRequest;
import com.org.paymentprocessor.dto.api.PaymentResponse;
import com.org.paymentprocessor.exception.DuplicatePaymentException;
import com.org.paymentprocessor.exception.InvalidDateRangeException;
import com.org.paymentprocessor.exception.PaymentProcessingException;
import com.org.paymentprocessor.integration.PaymentXmlTransformer;
import com.org.paymentprocessor.integration.XmlFileWriter;
import com.org.paymentprocessor.model.Payment;
import com.org.paymentprocessor.model.PaymentStatus;
import com.org.paymentprocessor.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentXmlTransformer paymentXmlTransformer;
    private final XmlFileWriter xmlFileWriter;

    private static final OffsetDateTime DEFAULT_FROM =
            OffsetDateTime.parse("0001-01-01T00:00:00Z");

    private static final OffsetDateTime DEFAULT_TO =
            OffsetDateTime.parse("9999-12-31T23:59:59Z");

    public List<PaymentResponse> getPayments(
            String customerId,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
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

    public PaymentResponse createPayment(PaymentRequest request) {
        if (paymentRepository.existsById(request.getId())) {
            throw new DuplicatePaymentException(request.getId());
        }

        Payment payment = Payment.builder()
                .id(request.getId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .timestamp(request.getTimestamp())
                .build();

        try {
            String xml = paymentXmlTransformer.transform(payment);
            xmlFileWriter.write(payment.getId(), xml);

            payment.setStatus(PaymentStatus.PROCESSED);
            payment.setProcessedAt(OffsetDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);

            return toResponse(savedPayment);
        } catch (PaymentProcessingException exception) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setProcessedAt(OffsetDateTime.now());

            paymentRepository.save(payment);

            throw exception;
        }
    }

}
