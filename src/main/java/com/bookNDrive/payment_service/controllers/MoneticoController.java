package com.bookNDrive.payment_service.controllers;

import com.bookNDrive.payment_service.configuration.MoneticoProperties;
import com.bookNDrive.payment_service.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class MoneticoController {


    private static final String URL_RETOUR_OK = "/";
    private static final String URL_RETOUR_KO = "/";
    private final PaymentService paymentService;
    private final MoneticoProperties moneticoProperties;


    @Autowired
    MoneticoController(PaymentService paymentService, MoneticoProperties monetico){
        this.paymentService = paymentService;
        this.moneticoProperties = monetico;
    }

    @PostMapping("/initier")
    public Map<String, String> generatePaymentForm(@RequestParam String id) {

        String temporaryMail = "khalfallah.razzak@gmail.com";

        Map<String, String> formParams = new HashMap<>();
        String date = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss"));

        System.out.println("la date + : " + date);
        String contexteBase64 = paymentService.contexteCommande();

        // Création des paramètres du formulaire
        formParams.put("version", moneticoProperties.version());
        formParams.put("TPE", moneticoProperties.tpe());
        formParams.put("date", date);
        formParams.put("montant", "5" + "EUR");
        formParams.put("reference", "23");
        formParams.put("lgue", "FR");
        formParams.put("societe", moneticoProperties.society());
        formParams.put("url_retour_ok", URL_RETOUR_OK);
        formParams.put("url_retour_err", URL_RETOUR_KO);
        formParams.put("texte-libre", "ceciestuntestdepaiement");
        formParams.put("mail", temporaryMail);
        formParams.put("contexte_commande", contexteBase64);

        // Calcul du sceau // a enregistrer en bdd pour pouvoir le vérifier sur lappel retour
        String dataToSign =
                "TPE=" + moneticoProperties.tpe()
                        + "*" + "contexte_commande=" + contexteBase64
                        + "*" + "date=" + date
                        + "*" + "lgue=FR"
                        + "*" + "mail=" + temporaryMail
                        + "*" + "montant=" + "5" + "EUR"
                        + "*" + "reference=" + "23"
                        + "*" + "societe=" + moneticoProperties.society()
                        + "*" + "texte-libre=ceciestuntestdepaiement"
                        + "*" + "url_retour_err=" + URL_RETOUR_KO
                        + "*" + "url_retour_ok=" + URL_RETOUR_OK
                        + "*" + "version=" + moneticoProperties.version();

        String mac = paymentService.generateMac(dataToSign, moneticoProperties.key());
        formParams.put("MAC", mac);

        return formParams;
    }


    @PostMapping("/retour")
    public String handlePaymentReturn(@RequestParam Map<String, String> params) {

        System.out.println("les params : " + params);
        String macRecu = params.get("MAC");

        String temporaryMail = "khalfallah.razzak@gmail.com";

        Map<String, String> formParams = new HashMap<>();
        String date = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss"));

        String contexteBase64 = paymentService.contexteCommande();
        String dataToValidate =
                "TPE=" + moneticoProperties.tpe()
                        + "*" + "contexte_commande=" + contexteBase64
                        + "*" + "date=" + date
                        + "*" + "lgue=FR"
                        + "*" + "mail=" + temporaryMail
                        + "*" + "montant=" + "5" + "EUR"
                        + "*" + "reference=" + "23"
                        + "*" + "societe=" + moneticoProperties.society()
                        + "*" + "texte-libre=ceciestuntestdepaiement"
                        + "*" + "url_retour_err=" + URL_RETOUR_KO
                        + "*" + "url_retour_ok=" + URL_RETOUR_OK
                        + "*" + "version=" + moneticoProperties.version();
        String macCalcul = paymentService.generateMac(dataToValidate, moneticoProperties.key());

        if (macRecu != null && macRecu.equals(macCalcul)) {
            if ("paiement".equals(params.get("code-retour")) || "payetest".equals(params.get("code-retour"))) {
                // Paiement accepté
                System.out.println("paiement accepté");
                return "version=2\ncdr=0\n";
            } else {
                // Paiement refusé
                System.out.println("paiement refusé");

                return "version=2\ncdr=1\n";
            }
        } else {
            // Signature invalide
            System.out.println("paiement refusé car signature invalide");

            return "version=2\ncdr=1\n";
        }
    }
}
