package com.bookNDrive.payment_service.exceptions;

import com.bookndrive.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends ApiException {

    public PaymentNotFoundException(String reference) {
        super(
                "Aucun paiement ne correspond a la reference " + reference,
                "PAYMENT_NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
    }
}
