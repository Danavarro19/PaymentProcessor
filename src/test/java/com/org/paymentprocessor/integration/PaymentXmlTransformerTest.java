package com.org.paymentprocessor.integration;

import com.org.paymentprocessor.model.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentXmlTransformerTest {

    private final PaymentXmlTransformer transformer =
            new PaymentXmlTransformer();

    @Test
    void shouldTransformPaymentToXml() {
        Payment payment = Payment.builder()
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

        String xml = transformer.transform(payment);

        assertThat(xml)
                .contains("<payment>")
                .contains("<id>PAY-1001</id>")
                .contains("<customerId>CUST-10</customerId>")
                .contains("<amount>125.50</amount>")
                .contains("<currency>USD</currency>")
                .contains(
                        "<timestamp>2026-09-16T10:00-06:00</timestamp>"
                )
                .contains("</payment>");
    }
}