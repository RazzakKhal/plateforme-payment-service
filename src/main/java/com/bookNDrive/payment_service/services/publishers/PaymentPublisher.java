package com.bookNDrive.payment_service.services.publishers;

import com.bookNDrive.payment_service.entities.Payment;

public interface PaymentPublisher {

    void publishPayment(Payment payment);
}
