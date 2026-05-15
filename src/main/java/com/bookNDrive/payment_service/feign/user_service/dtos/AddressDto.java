package com.bookNDrive.payment_service.feign.user_service.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class AddressDto {

    private UUID id;
    private String addressLine1;
    private String city;
    private String postalCode;
    private String country;
}
