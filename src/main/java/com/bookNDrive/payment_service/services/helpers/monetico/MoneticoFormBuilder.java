package com.bookNDrive.payment_service.services.helpers.monetico;

import com.bookNDrive.payment_service.configuration.MoneticoProperties;
import com.bookNDrive.payment_service.dtos.sended.PaymentFormDto;
import com.bookNDrive.payment_service.feign.user_service.dtos.UserDto;
import com.bookNDrive.payment_service.infrastructure.encoder.Encoder;
import com.bookNDrive.payment_service.mappers.PaymentMapper;
import com.bookNDrive.payment_service.models.ContexteCommande;
import com.bookNDrive.payment_service.models.Payment;
import com.bookNDrive.payment_service.services.KafkaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MoneticoFormBuilder {


    private final Encoder encoder;
    private final MoneticoProperties moneticoProperties;
    private static final String URL_RETOUR_OK = "https://ask-plateforme.fr/payment/success";
    private static final String URL_RETOUR_KO = "https://ask-plateforme.fr/payment/failed";

    @Autowired
    public MoneticoFormBuilder(
            Encoder encoder,
            MoneticoProperties moneticoProperties
    ){
        this.encoder =encoder;
        this.moneticoProperties = moneticoProperties;
    }


    public PaymentFormDto build(UserDto user, String price){
        String reference = Payment.generateReference();

        String date = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss"));

        String contexteBase64 = contexteCommande(user);

        String dataToSign =
                "3dsdebrayable=0"
                        + "*" + "TPE=" + moneticoProperties.tpe()
                        + "*" + "ThreeDSecureChallenge=challenge_preferred"
                        + "*" + "contexte_commande=" + contexteBase64
                        + "*" + "date=" + date
                        + "*" + "lgue=FR"
                        + "*" + "mail=" + user.getMail()
                        + "*" + "montant=" + price + "EUR"
                        + "*" + "reference=" + reference
                        + "*" + "societe=" + moneticoProperties.society()
                        + "*" + "texte-libre="
                        + "*" + "url_retour_err=" + URL_RETOUR_KO
                        + "*" + "url_retour_ok=" + URL_RETOUR_OK
                        + "*" + "version=" + moneticoProperties.version();

        var mac = generateMac(dataToSign, moneticoProperties.key());

        return new PaymentFormDto(
                date,
                user.getMail(),
                "0",  // 3dsdebrayable
                mac,
                moneticoProperties.tpe(),
                "challenge_preferred",  // ThreeDSecureChallenge
                contexteBase64,
                "FR",  // lgue
                price + "EUR",
                reference,
                moneticoProperties.society(),
                "",   // texte-libre
                URL_RETOUR_KO,
                URL_RETOUR_OK,
                moneticoProperties.version()
        );
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

    private String contexteCommande(UserDto user){
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

    public String generateMac(String dataToSign, String hexKey) {
        try {
            byte[] keyBytes = encoder.hexStringToByteArray(hexKey);

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));

            byte[] macBytes = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            return encoder.toHex(macBytes);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du calcul du HMAC", e);
        }
    }

}
