package com.bookNDrive.payment_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {

    private final StreamBridge streamBridge;

    public <T> boolean sendMessage(String output, T dto) {
        return streamBridge.send(output, dto);
    }
}
