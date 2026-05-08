package com.bookNDrive.payment_service.exceptions;

import com.bookndrive.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class PaymentInitializationException extends ApiException {

    public PaymentInitializationException(String message) {
        super(message, PaymentErrorCodes.PAYMENT_INITIALIZATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
