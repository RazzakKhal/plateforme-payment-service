package com.bookNDrive.payment_service.services;


import com.bookNDrive.payment_service.models.ContexteCommande;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    public String calculateHMAC(String data, String key) {
        try {
            Mac hmacSHA1 = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            hmacSHA1.init(secretKey);
            byte[] hash = hmacSHA1.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul du HMAC SHA1", e);
        }
    }

    public String contexteCommande(){
        Map<String, String> billing = Map.of(
                "firstName", "Razzak",
                "lastName", "Khalfallah",
                "addressLine1", "938 avenue des platanes",
                "city", "Lattes",
                "postalCode", "34970",
                "country", "FR"
        );


        ContexteCommande cc = new ContexteCommande(billing, null, null);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(cc);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'encodage du contexte_commande", e);
        }
    }



}
