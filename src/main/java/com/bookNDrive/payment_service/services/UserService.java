package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.feign.user_service.UserServiceFeignClient;
import com.bookNDrive.payment_service.feign.user_service.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private final UserServiceFeignClient userServiceFeignClient;

    public UserService(UserServiceFeignClient userServiceFeignClient){
        this.userServiceFeignClient = userServiceFeignClient;
    }

    public UserDto getCurrentUser(){
        var token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        System.out.println("token envoyé via getCurrentUser : "+token);

        return userServiceFeignClient.getUser("Bearer " +token).getBody();
    }

    public void saveUserFormula(Long formulaId){
        var token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        System.out.println("token envoyé via saveUserFormula : "+token);
        userServiceFeignClient.updateUserFormula(formulaId, "Bearer " +token);
    }
}
