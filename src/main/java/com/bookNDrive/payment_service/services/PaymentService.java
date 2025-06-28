package com.bookNDrive.payment_service.services;


import com.bookNDrive.payment_service.dtos.sended.PaymentDto;
import com.bookNDrive.payment_service.enums.PaymentStatus;
import com.bookNDrive.payment_service.feign.user_service.dtos.UserDto;
import com.bookNDrive.payment_service.mappers.PaymentMapper;
import com.bookNDrive.payment_service.models.ContexteCommande;
import com.bookNDrive.payment_service.models.Payment;
import com.bookNDrive.payment_service.repositories.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final KafkaService kafkaService;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, KafkaService kafkaService){
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.kafkaService = kafkaService;
    }

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

    public String contexteCommande(UserDto user){
        Map<String, String> billing = Map.of(
                "firstName", user.getFirstname(),
                "lastName", user.getLastname(),
                "addressLine1", user.getAddress().getAdressLine1(),
                "city", user.getAddress().getCity(),
                "postalCode", user.getAddress().getPostalCode(),
                "country", user.getAddress().getCountry()
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


    public String dataConstructFromMoneticoReturn(Map<String, String> params) {
        // On enlève le champ MAC
        Map<String, String> filtered = new HashMap<>(params);
        filtered.remove("MAC");

        // Trie alphabétique strict sur les clés
        return filtered.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("*"));
    }

    public Payment createPayment(String reference, Long userId, Long formulaId, String montant, String contexte, String mac) {

        Payment payment = new Payment();
        payment.setReference(reference);
        payment.setUserId(userId);
        payment.setFormulaId(formulaId);
        payment.setMontant(montant);
        payment.setMacEnvoye(mac);
        payment.setContexteCommande(contexte);

        return paymentRepository.save(payment);
    }

    public Payment markAsSuccess(String reference, String macRecu, Map<String, String> retourParams) {
        return paymentRepository.findByReference(reference)
                .map(p -> {
                    p.setMacRecu(macRecu);
                    p.setRawReturnParams(retourParams.toString());
                    p.setDateValidation(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Europe/Paris")));
                    p.setStatus(PaymentStatus.SUCCESS);
                    return paymentRepository.save(p);
                })
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec la référence : " + reference));
    }

    public void markAsInvalidSignature(String reference, String macRecu, Map<String, String> retourParams) {
        paymentRepository.findByReference(reference).ifPresent(p -> {
            p.setMacRecu(macRecu);
            p.setRawReturnParams(retourParams.toString());
            p.setStatus(PaymentStatus.INVALID_SIGNATURE);
            paymentRepository.save(p);
        });
    }

    public void markAsFailed(String reference, Map<String, String> retourParams) {
        paymentRepository.findByReference(reference).ifPresent(p -> {
            p.setRawReturnParams(retourParams.toString());
            p.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(p);
        });
    }

    public void saveUserFormulaFromPayment(Payment payment){
        var paymentDto = paymentMapper.paymentToPaymentDto(payment);
        kafkaService.sendMessage("sendCommunication-out-0", paymentDto);
    }
}
