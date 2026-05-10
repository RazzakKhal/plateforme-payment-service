package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.entities.Outbox;
import com.bookNDrive.payment_service.enums.EventDestination;
import com.bookNDrive.payment_service.enums.EventPublishStatus;
import com.bookNDrive.payment_service.events.Event;
import com.bookNDrive.payment_service.events.PaymentCreated;
import com.bookNDrive.payment_service.exceptions.ErrorsMessages;
import com.bookNDrive.payment_service.repositories.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaService kafkaService;

    public <T extends Event> void saveEventBeforePublishing(T event) throws JsonProcessingException {
        var outbox = new Outbox();
        outbox.setStatus(EventPublishStatus.PENDING);
        outbox.setEventName(event.getClass().getName());
        outbox.setPayload(objectMapper.writeValueAsString(event));
        outboxRepository.save(outbox);
        log.info(
                "Evenement outbox cree outboxId={} eventName={} status={}",
                outbox.getId(),
                event.getClass().getSimpleName(),
                outbox.getStatus()
        );
    }

    @Transactional
    public void processPendingEvents() {
        var pendingEvents = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(EventPublishStatus.PENDING);
        if (pendingEvents.isEmpty()) {
            return;
        }

        int publishedCount = 0;
        int failedCount = 0;
        log.info("Traitement outbox demarre pendingCount={}", pendingEvents.size());

        for (Outbox outbox : pendingEvents) {
            try {
                boolean sent = publish(outbox);

                if (sent) {
                    outbox.setStatus(EventPublishStatus.PUBLISHED);
                    outbox.setPublishedAt(LocalDateTime.now());
                    publishedCount++;
                    log.info(
                            "Evenement outbox publie outboxId={} eventName={} status={}",
                            outbox.getId(),
                            outbox.getEventName(),
                            outbox.getStatus()
                    );
                } else {
                    markAsFailed(outbox, ErrorsMessages.KAFKA_SEND_ERROR);
                    failedCount++;
                }

            } catch (Exception ex) {
                markAsFailed(outbox, ex.getMessage());
                failedCount++;
            }

            outboxRepository.save(outbox);
        }

        log.info(
                "Traitement outbox termine pendingCount={} publishedCount={} failedCount={}",
                pendingEvents.size(),
                publishedCount,
                failedCount
        );
    }

    private boolean publish(Outbox outbox) throws JsonProcessingException {
        if (PaymentCreated.class.getName().equals(outbox.getEventName())) {
            var event = objectMapper.readValue(
                    outbox.getPayload(),
                    PaymentCreated.class
            );

            log.info(
                    "Publication Kafka demandee outboxId={} destination={} userId={} formulaId={}",
                    outbox.getId(),
                    EventDestination.SEND_COMMUNICATION.getDestination(),
                    event.paymentDto().getUserId(),
                    event.paymentDto().getFormulaId()
            );
            return kafkaService.sendMessage(
                    EventDestination.SEND_COMMUNICATION.getDestination(),
                    event.paymentDto()
            );
        }

        throw new IllegalArgumentException(
                "Unsupported event type: " + outbox.getEventName()
        );
    }

    private void markAsFailed(Outbox outbox, String errorMessage) {
        outbox.setStatus(EventPublishStatus.FAILED);
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        outbox.setLastError(errorMessage);
        log.warn(
                "Publication outbox en echec outboxId={} eventName={} retryCount={} error={}",
                outbox.getId(),
                outbox.getEventName(),
                outbox.getRetryCount(),
                errorMessage
        );
    }
}
