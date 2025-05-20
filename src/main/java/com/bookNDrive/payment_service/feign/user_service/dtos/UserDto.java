package com.bookNDrive.payment_service.feign.user_service.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String mail;
    private Long formulaId;
    private Set<Role> roles;
    private AdressDto address;
}