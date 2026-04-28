package com.Kee.V2C.exception;

public class BadPaymentInfo extends RuntimeException {
    public BadPaymentInfo(String message) {
        super(message);
    }
}
