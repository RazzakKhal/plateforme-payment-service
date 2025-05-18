package com.bookNDrive.payment_service.feign.dtos;

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

    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}