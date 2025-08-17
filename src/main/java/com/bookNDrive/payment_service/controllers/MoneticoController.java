package com.bookNDrive.payment_service.controllers;

import com.bookNDrive.payment_service.configuration.MoneticoProperties;
import com.bookNDrive.payment_service.services.FormulaService;
import com.bookNDrive.payment_service.services.PaymentService;
import com.bookNDrive.payment_service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
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
import java.util.UUID;

@RestController
@RequestMapping("/payment")
public class MoneticoController {


    private static final String URL_RETOUR_OK = "/";
    private static final String URL_RETOUR_KO = "/";
    private final PaymentService paymentService;
    private final MoneticoProperties moneticoProperties;
    private final UserService userService;
    private final FormulaService formulaService;


    @Autowired
    MoneticoController(PaymentService paymentService, UserService userService, MoneticoProperties monetico, FormulaService formulaService){
        this.paymentService = paymentService;
        this.userService = userService;
        this.moneticoProperties = monetico;
        this.formulaService = formulaService;
    }

    @PostMapping("/initier")
    public Map<String, String> generatePaymentForm(@RequestParam String formulaId) {

        String reference = UUID.randomUUID().toString().replace("-", "").substring(0, 12);


        var user = userService.getCurrentUser();
        var formula = formulaService.getFormulaById(Long.valueOf(formulaId));
        var price = formulaService.getPrice(formula);
        System.out.println("récuperation reussie + " + user.getMail());

        // tout ca à mettre dans un service à part
        String temporaryMail = user.getMail();

        Map<String, String> formParams = new HashMap<>();
        String date = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss"));

        String contexteBase64 = paymentService.contexteCommande(user);

        // Création des paramètres du formulaire
        formParams.put("version", moneticoProperties.version());
        formParams.put("TPE", moneticoProperties.tpe());
        formParams.put("date", date);
        formParams.put("montant", formulaService.getPrice(formula) + "EUR");
        formParams.put("reference", reference);
        formParams.put("lgue", "FR");
        formParams.put("societe", moneticoProperties.society());
        formParams.put("url_retour_ok", URL_RETOUR_OK);
        formParams.put("url_retour_err", URL_RETOUR_KO);
        formParams.put("texte-libre", "");
        formParams.put("mail", user.getMail());
        formParams.put("contexte_commande", contexteBase64);

        formParams.put("3dsdebrayable", "0");
        formParams.put("ThreeDSecureChallenge", "challenge_preferred");

        // Calcul du sceau // a enregistrer en bdd pour pouvoir le vérifier sur lappel retour
        String dataToSign =
                "3dsdebrayable=0"
                        + "*" + "TPE=" + moneticoProperties.tpe()
                        + "*" + "ThreeDSecureChallenge=challenge_preferred"
                        + "*" + "contexte_commande=" + contexteBase64
                        + "*" + "date=" + date
                        + "*" + "lgue=FR"
                        + "*" + "mail=" + user.getMail()
                        + "*" + "montant=" + formulaService.getPrice(formula) + "EUR"
                        + "*" + "reference=" + reference
                        + "*" + "societe=" + moneticoProperties.society()
                        + "*" + "texte-libre="
                        + "*" + "url_retour_err=" + URL_RETOUR_KO
                        + "*" + "url_retour_ok=" + URL_RETOUR_OK
                        + "*" + "version=" + moneticoProperties.version();

        String mac = paymentService.generateMac(dataToSign, moneticoProperties.key());
        formParams.put("MAC", mac);

        paymentService.createPayment(reference,user.getId(),formula.getId(),price, contexteBase64,mac);

        return formParams;
    }


    @PostMapping("/retour")
    public String handlePaymentReturn(@RequestParam Map<String, String> params) {

        System.out.println("les params : " + params);
        String macRecu = params.get("MAC");
        var reference = params.get("reference");
        // récupérer par referance le paiement pour modifier
        System.out.println("authentification 3ds : " + params.get("authentification"));
        String dataToValidate = paymentService.dataConstructFromMoneticoReturn(params);

        String macCalcul = paymentService.generateMac(dataToValidate, moneticoProperties.key());

        if (macCalcul.equalsIgnoreCase(macRecu)) {
            if ("paiement".equals(params.get("code-retour")) || "payetest".equals(params.get("code-retour"))) {
                // Paiement accepté
                System.out.println("paiement accepté");
                var payment = paymentService.markAsSuccess(reference,macRecu,params);
                paymentService.saveUserFormulaFromPayment(payment);
                return "version=2\ncdr=0\n";
            } else {
                // Paiement refusé
                System.out.println("paiement refusé");
                paymentService.markAsFailed(reference,params);
                return "version=2\ncdr=1\n";
            }
        } else {
            // Signature invalide
            System.out.println("paiement refusé car signature invalide");
            paymentService.markAsInvalidSignature(reference,macRecu,params);
            return "version=2\ncdr=1\n";
        }
    }
}
