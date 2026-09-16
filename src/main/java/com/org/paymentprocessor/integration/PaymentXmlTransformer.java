package com.org.paymentprocessor.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.org.paymentprocessor.dto.core.CorePaymentXml;
import com.org.paymentprocessor.exception.PaymentProcessingException;
import com.org.paymentprocessor.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentXmlTransformer {

    private final XmlMapper xmlMapper;

    public PaymentXmlTransformer() {
        this.xmlMapper = XmlMapper.builder()
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .build();
    }

    public String transform(Payment payment) {
        CorePaymentXml corePaymentXml = new CorePaymentXml(
                payment.getId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getTimestamp().toString()
        );

        try {
            return xmlMapper.writeValueAsString(corePaymentXml);
        } catch (JsonProcessingException exception) {
            throw new PaymentProcessingException(
                    "Could not transform payment "
                            + payment.getId()
                            + " to XML",
                    exception
            );
        }
    }
}