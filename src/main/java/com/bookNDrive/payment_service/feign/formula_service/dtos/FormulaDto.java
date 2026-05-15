package com.bookNDrive.payment_service.feign.formula_service.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class FormulaDto {

    private UUID id;
    private String title;
    private String description;
    private Double price;
    private boolean code;
    private Double promotionnalPrice;
}
