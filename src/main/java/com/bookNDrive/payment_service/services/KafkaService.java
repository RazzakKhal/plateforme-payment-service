package com.bookNDrive.payment_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {

    private final StreamBridge streamBridge;

    @Autowired
    public KafkaService(StreamBridge streamBridge){
        this.streamBridge = streamBridge;
    }

    public <T> boolean sendMessage(String output, T dto){
        return streamBridge.send(output, dto);
    }
}
