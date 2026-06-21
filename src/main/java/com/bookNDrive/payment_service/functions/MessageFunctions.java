package com.bookNDrive.payment_service.functions;

import com.bookNDrive.payment_service.events.FormulaSavedEvent;
import com.bookNDrive.payment_service.services.PaymentCompletionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class MessageFunctions {

    @Bean
    public Consumer<FormulaSavedEvent> completePayment(PaymentCompletionService paymentCompletionService) {
        return event -> {
            log.info("Message Kafka recu pour la completion de paiement reference={}", event.reference());
            paymentCompletionService.completePayment(event.reference());
        };
    }
}
