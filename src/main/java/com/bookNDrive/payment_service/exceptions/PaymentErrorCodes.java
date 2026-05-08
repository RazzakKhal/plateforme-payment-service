package com.bookNDrive.payment_service.exceptions;

public final class PaymentErrorCodes {

    public static final String PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND";
    public static final String INVALID_PAYMENT_CALLBACK = "INVALID_PAYMENT_CALLBACK";
    public static final String PAYMENT_INITIALIZATION_ERROR = "PAYMENT_INITIALIZATION_ERROR";
    public static final String UPSTREAM_SERVICE_ERROR = "UPSTREAM_SERVICE_ERROR";

    private PaymentErrorCodes() {
    }
}
