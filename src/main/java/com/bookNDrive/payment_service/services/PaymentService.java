package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.dtos.sended.PaymentFormDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.util.UUID;

public interface PaymentService {

    PaymentFormDto createPayment(UUID formulaId);

    String paymentStatus(Map<String, String> returnParameters) throws JsonProcessingException;
}
