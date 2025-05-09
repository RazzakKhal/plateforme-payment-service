package com.bookNDrive.payment_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monetico")
public record MoneticoProperties(
        String tpe,
        String version,
        String society,
        String key,
        String paymentUrl,
        String returnUrl
) {}