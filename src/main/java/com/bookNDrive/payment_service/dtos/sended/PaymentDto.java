package com.bookNDrive.payment_service.dtos.sended;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentDto {

    private String status;

    private UUID userId;

    private UUID formulaId;
}
