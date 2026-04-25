package com.bookNDrive.payment_service.repositories;


import com.bookNDrive.payment_service.entities.Outbox;
import com.bookNDrive.payment_service.enums.EventPublishStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {

    List<Outbox> findTop50ByStatusOrderByCreatedAtAsc(EventPublishStatus status);
}
