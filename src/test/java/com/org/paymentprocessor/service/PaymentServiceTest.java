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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentXmlTransformer paymentXmlTransformer;

    @Mock
    private XmlFileWriter xmlFileWriter;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldCreatePaymentSuccessfully() {
        PaymentRequest request = paymentRequest();

        when(paymentRepository.existsById("PAY-1001"))
                .thenReturn(false);

        when(paymentXmlTransformer.transform(any(Payment.class)))
                .thenReturn("<payment/>");

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.createPayment(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("PAY-1001");
        assertThat(response.getCustomerId()).isEqualTo("CUST-10");
        assertThat(response.getAmount())
                .isEqualByComparingTo("125.50");
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getStatus())
                .isEqualTo(PaymentStatus.PROCESSED);
        assertThat(response.getProcessedAt()).isNotNull();

        verify(paymentRepository).existsById("PAY-1001");
        verify(paymentXmlTransformer)
                .transform(any(Payment.class));
        verify(xmlFileWriter)
                .write("PAY-1001", "<payment/>");
        verify(paymentRepository)
                .save(any(Payment.class));
    }

    @Test
    void shouldRejectDuplicatePayment() {
        PaymentRequest request = paymentRequest();

        when(paymentRepository.existsById("PAY-1001"))
                .thenReturn(true);

        assertThatThrownBy(
                () -> paymentService.createPayment(request)
        )
                .isInstanceOf(DuplicatePaymentException.class);

        verify(paymentRepository)
                .existsById("PAY-1001");

        verifyNoInteractions(
                paymentXmlTransformer,
                xmlFileWriter
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void shouldMarkPaymentAsFailedWhenXmlProcessingFails() {
        PaymentRequest request = paymentRequest();

        when(paymentRepository.existsById("PAY-1001"))
                .thenReturn(false);

        when(paymentXmlTransformer.transform(any(Payment.class)))
                .thenThrow(
                        new PaymentProcessingException(
                                "Could not transform payment"
                        )
                );

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(
                () -> paymentService.createPayment(request)
        )
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessage("Could not transform payment");

        ArgumentCaptor<Payment> captor =
                ArgumentCaptor.forClass(Payment.class);

        verify(paymentRepository).save(captor.capture());

        Payment failedPayment = captor.getValue();

        assertThat(failedPayment.getStatus())
                .isEqualTo(PaymentStatus.FAILED);

        assertThat(failedPayment.getProcessedAt())
                .isNotNull();

        verifyNoInteractions(xmlFileWriter);
    }

    @Test
    void shouldReturnPaymentsWithoutCustomerFilter() {
        OffsetDateTime from =
                OffsetDateTime.parse("2026-09-01T00:00:00Z");

        OffsetDateTime to =
                OffsetDateTime.parse("2026-09-30T23:59:59Z");

        when(paymentRepository.findByTimestampBetween(from, to))
                .thenReturn(List.of(payment()));

        List<PaymentResponse> result =
                paymentService.getPayments(null, from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId())
                .isEqualTo("PAY-1001");

        verify(paymentRepository)
                .findByTimestampBetween(from, to);

        verify(paymentRepository, never())
                .findByCustomerIdAndTimestampBetween(
                        anyString(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldReturnPaymentsForCustomer() {
        OffsetDateTime from =
                OffsetDateTime.parse("2026-09-01T00:00:00Z");

        OffsetDateTime to =
                OffsetDateTime.parse("2026-09-30T23:59:59Z");

        when(
                paymentRepository
                        .findByCustomerIdAndTimestampBetween(
                                "CUST-10",
                                from,
                                to
                        )
        ).thenReturn(List.of(payment()));

        List<PaymentResponse> result =
                paymentService.getPayments(
                        "CUST-10",
                        from,
                        to
                );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId())
                .isEqualTo("CUST-10");

        verify(paymentRepository)
                .findByCustomerIdAndTimestampBetween(
                        "CUST-10",
                        from,
                        to
                );

        verify(paymentRepository, never())
                .findByTimestampBetween(any(), any());
    }

    @Test
    void shouldRejectInvalidDateRange() {
        OffsetDateTime from =
                OffsetDateTime.parse("2026-09-30T00:00:00Z");

        OffsetDateTime to =
                OffsetDateTime.parse("2026-09-01T00:00:00Z");

        assertThatThrownBy(
                () -> paymentService.getPayments(
                        null,
                        from,
                        to
                )
        )
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessage(
                        "'from' must be earlier than or equal to 'to'"
                );

        verifyNoInteractions(paymentRepository);
    }

    private PaymentRequest paymentRequest() {
        return PaymentRequest.builder()
                .id("PAY-1001")
                .customerId("CUST-10")
                .amount(new BigDecimal("125.50"))
                .currency("USD")
                .timestamp(
                        OffsetDateTime.parse(
                                "2026-09-16T10:00:00-06:00"
                        )
                )
                .build();
    }

    private Payment payment() {
        return Payment.builder()
                .id("PAY-1001")
                .customerId("CUST-10")
                .amount(new BigDecimal("125.50"))
                .currency("USD")
                .timestamp(
                        OffsetDateTime.parse(
                                "2026-09-16T10:00:00-06:00"
                        )
                )
                .status(PaymentStatus.PROCESSED)
                .processedAt(
                        OffsetDateTime.parse(
                                "2026-09-16T10:00:01-06:00"
                        )
                )
                .build();
    }
}