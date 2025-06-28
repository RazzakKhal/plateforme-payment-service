package com.bookNDrive.payment_service.dtos.sended;

import lombok.Data;

@Data
public class PaymentDto {

    private String status;

    private Long userId;

    private Long formulaId;
}
