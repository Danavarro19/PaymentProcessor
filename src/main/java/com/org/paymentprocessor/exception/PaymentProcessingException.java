package com.org.paymentprocessor.exception;

import com.org.paymentprocessor.model.Payment;

public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
