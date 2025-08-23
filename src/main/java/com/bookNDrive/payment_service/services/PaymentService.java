package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.dtos.sended.PaymentFormDto;
import java.util.Map;


public interface PaymentService {

   PaymentFormDto createPayment(String formulaId);

   String paymentStatus(Map<String, String> returnParameters);

}
