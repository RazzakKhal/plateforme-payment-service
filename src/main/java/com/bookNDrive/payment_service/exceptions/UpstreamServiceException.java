package com.bookNDrive.payment_service.exceptions;

import com.bookndrive.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class UpstreamServiceException extends ApiException {

    public UpstreamServiceException(String message) {
        super(message, PaymentErrorCodes.UPSTREAM_SERVICE_ERROR, HttpStatus.BAD_GATEWAY.value());
    }
}
