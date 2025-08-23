package com.bookNDrive.payment_service.services.publishers.monetico;

import com.bookNDrive.payment_service.mappers.PaymentMapper;
import com.bookNDrive.payment_service.models.Payment;
import com.bookNDrive.payment_service.services.KafkaService;
import com.bookNDrive.payment_service.services.publishers.PaymentPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentMoneticoPublisher implements PaymentPublisher {

    private final KafkaService kafkaService;
    private final PaymentMapper paymentMapper;

    @Autowired
    PaymentMoneticoPublisher(
            PaymentMapper paymentMapper,
            KafkaService kafkaService
    ){
        this.kafkaService = kafkaService;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public void publishPayment(Payment payment) {
        var paymentDto = paymentMapper.paymentToPaymentDto(payment);
        kafkaService.sendMessage("sendCommunication-out-0", paymentDto);
    }
}
