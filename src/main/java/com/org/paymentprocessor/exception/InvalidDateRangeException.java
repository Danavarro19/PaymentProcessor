package com.org.paymentprocessor.exception;

public class InvalidDateRangeException extends RuntimeException{
    public InvalidDateRangeException(String message) {
        super(message);
    }
}
