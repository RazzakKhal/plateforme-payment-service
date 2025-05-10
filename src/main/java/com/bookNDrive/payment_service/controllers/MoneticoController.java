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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class MoneticoController {


    private static final String URL_RETOUR_OK = "/";
    private static final String URL_RETOUR_KO = "/";
    private final PaymentService paymentService;
    private final MoneticoProperties moneticoProperties;

    @Value("${monetico.key}")
    private String test;

    @Autowired
    MoneticoController(PaymentService paymentService, MoneticoProperties monetico){
        this.paymentService = paymentService;
        this.moneticoProperties = monetico;
    }

    @PostMapping("/initier")
    public Map<String, String> generatePaymentForm(@RequestParam String id) {

        System.out.println("ceci est la key : " + test);

        String temporaryMail = "khalfallah.razzak@gmail.com";

        Map<String, String> formParams = new HashMap<>();
        String date = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss").format(new Date());

        String contexteBase64 = paymentService.contexteCommande();

        // Création des paramètres du formulaire
        formParams.put("version", moneticoProperties.version());
        formParams.put("TPE", moneticoProperties.tpe());
        formParams.put("date", date);
        formParams.put("montant", "5" + "EUR");
        formParams.put("reference", id);
        formParams.put("lgue", "FR");
        formParams.put("societe", moneticoProperties.society());
        formParams.put("url_retour_ok", URL_RETOUR_OK);
        formParams.put("url_retour_err", URL_RETOUR_KO);
        formParams.put("texte-libre", "ceciestuntestdepaiement");
        formParams.put("mail", temporaryMail);
        formParams.put("contexte_commande", contexteBase64);

        // Calcul du sceau
        String dataToSign =
                "TPE=" + moneticoProperties.tpe()
                        + "*" + "contexte_commande=" + contexteBase64
                        + "*" + "date=" + date
                        + "*" + "lgue=FR"
                        + "*" + "mail=" + temporaryMail
                        + "*" + "montant=" + "5" + "EUR"
                        + "*" + "reference=" + id
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
        String macRecu = params.get("MAC");
        String dataToValidate = params.get("TPE") + "*" + params.get("date") + "*" + params.get("montant") + "*" + params.get("reference") + "*FR*" + params.get("societe") + "*";
        String macCalcul = paymentService.generateMac(dataToValidate, moneticoProperties.key());

        if (macRecu != null && macRecu.equals(macCalcul)) {
            if ("paiement".equals(params.get("code-retour"))) {
                // Paiement accepté
                return "version=2\ncdr=0";
            } else {
                // Paiement refusé
                return "version=2\ncdr=1";
            }
        } else {
            // Signature invalide
            return "version=2\ncdr=1";
        }
    }
}
