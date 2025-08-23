package com.bookNDrive.payment_service.models;

import com.bookNDrive.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Data
public class Payment {

    private final ZonedDateTime dateInitiation = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Europe/Paris"));
    private final String currency = "EUR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String reference;

    private Long userId;

    private Long formulaId;

    private String montant;

    private String macEnvoye;
    private String macRecu;
    private String contexteCommande;
    private ZonedDateTime dateValidation;


    @Column(columnDefinition = "text")
    private String rawReturnParams;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;


}

