package com.bookNDrive.payment_service.services;


import com.bookNDrive.payment_service.interfaces.Scheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OutboxScheduler implements Scheduler {

    private final OutboxService outboxService;

    @Override
    @Scheduled(fixedDelay = 5000)
    public void schedule() {
        outboxService.processPendingEvents();
    }
}

