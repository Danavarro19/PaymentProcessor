package com.org.paymentprocessor.dto.core;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;

@JacksonXmlRootElement(localName = "payment")
public record CorePaymentXml(
        @JacksonXmlProperty(localName = "id")
        String id,

        @JacksonXmlProperty(localName = "customerId")
        String customerId,

        @JacksonXmlProperty(localName = "amount")
        BigDecimal amount,

        @JacksonXmlProperty(localName = "currency")
        String currency,

        @JacksonXmlProperty(localName = "timestamp")
        String timestamp
) {
}