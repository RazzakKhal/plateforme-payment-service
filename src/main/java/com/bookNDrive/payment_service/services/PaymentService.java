package com.bookNDrive.payment_service.services;


import com.bookNDrive.payment_service.models.ContexteCommande;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    public String generateMac(String dataToSign, String hexKey) {
        try {
            // Convertir la clé hexadécimale en tableau de bytes
            byte[] keyBytes = hexStringToByteArray(hexKey);

            // Créer l'instance HmacSHA1
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));

            // Calculer le MAC sur les données
            byte[] macBytes = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));

            // Afficher en Base64 (à envoyer à Monetico)
            String macBase64 = Base64.getEncoder().encodeToString(macBytes);

            return toHex(macBytes);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul du HMAC", e);
        }
    }

    // Convertit une chaîne hexadécimale en tableau de bytes
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    // Convertit des bytes en chaîne hexadécimale MAJUSCULE
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));  // %02X = deux chiffres hex, majuscules
        }
        return sb.toString();
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
