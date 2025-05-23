package com.bookNDrive.payment_service.feign.user_service;

import com.bookNDrive.payment_service.feign.user_service.dtos.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient("user-service")
public interface UserServiceFeignClient {


    @GetMapping("/users/me")
    public ResponseEntity<UserDto> getUser(@RequestHeader("Authorization") String bearerToken);

    @PutMapping("/users/formula")
    public ResponseEntity<Void> updateUserFormula(@RequestParam("formulaId") Long formulaId, @RequestHeader("Authorization") String bearerToken);
}
