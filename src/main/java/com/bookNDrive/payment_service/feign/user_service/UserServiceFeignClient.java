package com.bookNDrive.payment_service.feign.user_service;

import com.bookNDrive.payment_service.feign.user_service.dtos.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserServiceFeignClient {


    @GetMapping("/users/me")
    public ResponseEntity<UserDto> getUser(@RequestHeader("Authorization") String bearerToken);

    @PatchMapping("/users/formula")
    void updateUserFormula(@RequestParam("formulaId") Long formulaId);
}
