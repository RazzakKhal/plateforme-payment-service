package com.bookNDrive.payment_service.feign.formula_service.dtos;

import lombok.Data;

@Data
public class FormulaDto {

    private Long id;
    private String title;
    private String description;
    private Double price;
    private boolean code;
    private Double promotionnalPrice;
}
