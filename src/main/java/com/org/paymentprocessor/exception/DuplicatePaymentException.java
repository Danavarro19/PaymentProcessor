package com.org.paymentprocessor.exception;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(String paymentId) {
        super("Payment already exists with id: " + paymentId);
    }

}
