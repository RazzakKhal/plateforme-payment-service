package com.bookNDrive.payment_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaService {

    private final StreamBridge streamBridge;

    public <T> boolean sendMessage(String output, T dto) {
        boolean sent = streamBridge.send(output, dto);
        if (sent) {
            log.info(
                    "Message envoye vers Kafka destination={} payloadType={}",
                    output,
                    dto != null ? dto.getClass().getSimpleName() : "null"
            );
        } else {
            log.warn(
                    "Envoi Kafka refuse destination={} payloadType={}",
                    output,
                    dto != null ? dto.getClass().getSimpleName() : "null"
            );
        }
        return sent;
    }
}
