package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.dtos.sended.PaymentDto;
import com.bookNDrive.payment_service.feign.user_service.UserServiceFeignClient;
import com.bookNDrive.payment_service.feign.user_service.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserServiceFeignClient userServiceFeignClient;

    private final  KafkaService kafkaService;

    @Autowired
    public UserService(UserServiceFeignClient userServiceFeignClient, KafkaService kafkaService){
        this.userServiceFeignClient = userServiceFeignClient;
        this.kafkaService = kafkaService;
    }

    public UserDto getCurrentUser(){
        var token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        System.out.println("token envoyé via getCurrentUser : "+token);

        return userServiceFeignClient.getUser("Bearer " +token).getBody();
    }


}
