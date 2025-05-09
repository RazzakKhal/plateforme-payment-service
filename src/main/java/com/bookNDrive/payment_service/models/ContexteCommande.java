package com.bookNDrive.payment_service.models;

import java.util.Map;

public record ContexteCommande(
        Map<String, String> billing,
        Map<String, Object> shipping,
        Map<String, Object> client
) {}