package com.bookNDrive.payment_service.entities;

import com.bookNDrive.payment_service.enums.EventPublishStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String eventName;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private EventPublishStatus status;

    private int retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    private String lastError;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
