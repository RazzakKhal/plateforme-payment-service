package com.bookNDrive.payment_service.entities;

import com.bookNDrive.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "payments")
public class Payment extends BaseEntity {

    private final ZonedDateTime dateInitiation = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Europe/Paris"));
    private final String currency = "EUR";


    private String reference;

    private UUID userId;

    private UUID formulaId;

    private String montant;

    private String macEnvoye;
    private String macRecu;
    private String contexteCommande;
    private ZonedDateTime dateValidation;


    @Column(columnDefinition = "text")
    private String rawReturnParams;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public static String generateReference() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }


}

