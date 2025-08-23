package com.bookNDrive.payment_service.services.helpers.monetico;

import com.bookNDrive.payment_service.configuration.MoneticoProperties;
import com.bookNDrive.payment_service.enums.PaymentStatus;
import com.bookNDrive.payment_service.models.Payment;
import com.bookNDrive.payment_service.repositories.PaymentRepository;
import com.bookNDrive.payment_service.services.publishers.PaymentPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Component
public class MoneticoStatusHandler {

    private final PaymentRepository paymentRepository;
    private final MoneticoFormBuilder moneticoBuilder;
    private final MoneticoProperties moneticoProperties;
    private final PaymentPublisher paymentPublisher;

    @Autowired
    MoneticoStatusHandler(
            PaymentRepository paymentRepository,
            MoneticoFormBuilder moneticoBuilder,
            PaymentPublisher paymentPublisher,
            MoneticoProperties moneticoProperties){
        this.paymentRepository = paymentRepository;
        this.moneticoBuilder = moneticoBuilder;
        this.paymentPublisher = paymentPublisher;
        this.moneticoProperties = moneticoProperties;
    }

    public String paymentStatus(Map<String, String> returnParameters){
        var macRecu = returnParameters.get("MAC");
        var reference = returnParameters.get("reference");

        var dataToValidate = moneticoBuilder.dataConstructFromMoneticoReturn(returnParameters);

        var macCalcul = moneticoBuilder.generateMac(dataToValidate, moneticoProperties.key());


        if (macCalcul.equalsIgnoreCase(macRecu)) {
            if ("paiement".equals(returnParameters.get("code-retour")) || "payetest".equals(returnParameters.get("code-retour"))) {

                var payment = markAsSuccess(reference,macRecu,returnParameters);
                paymentPublisher.publishPayment(payment);
                return "version=2\ncdr=0\n";
            } else {

                markAsFailed(reference,returnParameters);
                return "version=2\ncdr=1\n";
            }
        } else {

            markAsInvalidSignature(reference,macRecu,returnParameters);
            return "version=2\ncdr=1\n";
        }
    }


    private Payment markAsSuccess(String reference, String macRecu, Map<String, String> retourParams) {
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

    private void markAsInvalidSignature(String reference, String macRecu, Map<String, String> retourParams) {
        paymentRepository.findByReference(reference).ifPresent(p -> {
            p.setMacRecu(macRecu);
            p.setRawReturnParams(retourParams.toString());
            p.setStatus(PaymentStatus.INVALID_SIGNATURE);
            paymentRepository.save(p);
        });
    }

    private void markAsFailed(String reference, Map<String, String> retourParams) {
        paymentRepository.findByReference(reference).ifPresent(p -> {
            p.setRawReturnParams(retourParams.toString());
            p.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(p);
        });
    }
}
