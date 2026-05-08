package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.exceptions.UpstreamServiceException;
import com.bookNDrive.payment_service.feign.user_service.UserServiceFeignClient;
import com.bookNDrive.payment_service.feign.user_service.dtos.UserDto;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserServiceFeignClient userServiceFeignClient;

    @Autowired
    public UserService(UserServiceFeignClient userServiceFeignClient, KafkaService kafkaService) {
        this.userServiceFeignClient = userServiceFeignClient;
    }

    public UserDto getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new UpstreamServiceException("Aucun jeton utilisateur n'est disponible pour l'appel au user-service");
        }

        String token = authentication.getCredentials().toString();

        try {
            var response = userServiceFeignClient.getUser("Bearer " + token);
            if (response.getBody() == null) {
                throw new UpstreamServiceException("Le user-service n'a retourne aucun utilisateur");
            }
            return response.getBody();
        } catch (FeignException ex) {
            throw new UpstreamServiceException("Le user-service est indisponible ou a refuse la requete");
        }
    }
}
