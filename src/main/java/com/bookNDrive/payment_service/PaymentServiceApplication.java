package com.bookNDrive.payment_service;

import com.bookNDrive.payment_service.configuration.MoneticoProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(MoneticoProperties.class)
@EnableFeignClients
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title = "Payment Service",
                description = "Microservice responsable des paiements"
        )
)
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

}
