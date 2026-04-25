package com.bookNDrive.payment_service.events;

import com.bookNDrive.payment_service.dtos.sended.PaymentDto;

public record PaymentCreated(PaymentDto paymentDto) implements Event {
}
