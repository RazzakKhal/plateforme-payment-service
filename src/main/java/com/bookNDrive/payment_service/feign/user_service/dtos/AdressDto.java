package com.bookNDrive.payment_service.feign.user_service.dtos;

import lombok.Data;

@Data
public class AdressDto {

        private Long id;
        private String adressLine1;
        private String city;
        private String postalCode;
        private String country;


}
