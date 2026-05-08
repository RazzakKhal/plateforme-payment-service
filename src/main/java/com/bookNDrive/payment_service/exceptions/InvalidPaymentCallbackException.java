package com.bookNDrive.payment_service.exceptions;

import com.bookndrive.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPaymentCallbackException extends ApiException {

    public InvalidPaymentCallbackException(String message) {
        super(message, PaymentErrorCodes.INVALID_PAYMENT_CALLBACK, HttpStatus.BAD_REQUEST.value());
    }
}
