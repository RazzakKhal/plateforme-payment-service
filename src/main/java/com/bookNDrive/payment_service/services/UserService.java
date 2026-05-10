package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.exceptions.UpstreamServiceException;
import com.bookNDrive.payment_service.feign.user_service.UserServiceFeignClient;
import com.bookNDrive.payment_service.feign.user_service.dtos.UserDto;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    private final UserServiceFeignClient userServiceFeignClient;

    @Autowired
    public UserService(UserServiceFeignClient userServiceFeignClient, KafkaService kafkaService) {
        this.userServiceFeignClient = userServiceFeignClient;
    }

    public UserDto getCurrentUser() {
        log.info("Appel au user-service initie pour recuperer l'utilisateur courant");
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            log.warn("Appel au user-service impossible, jeton utilisateur absent");
            throw new UpstreamServiceException("Aucun jeton utilisateur n'est disponible pour l'appel au user-service");
        }

        String token = authentication.getCredentials().toString();

        try {
            var response = userServiceFeignClient.getUser("Bearer " + token);
            if (response.getBody() == null) {
                log.error("Le user-service a repondu sans corps");
                throw new UpstreamServiceException("Le user-service n'a retourne aucun utilisateur");
            }
            log.info("Appel au user-service reussi userId={}", response.getBody().getId());
            return response.getBody();
        } catch (FeignException ex) {
            log.error("Echec de l'appel au user-service", ex);
            throw new UpstreamServiceException("Le user-service est indisponible ou a refuse la requete");
        }
    }
}
