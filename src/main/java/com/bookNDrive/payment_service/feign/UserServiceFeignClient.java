package com.bookNDrive.payment_service.feign;

import com.bookNDrive.payment_service.feign.dtos.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient("user-service")
public interface UserServiceFeignClient {


    @GetMapping("/users/me")
    public ResponseEntity<UserDto> getUser(@RequestHeader("Authorization") String bearerToken);
}
