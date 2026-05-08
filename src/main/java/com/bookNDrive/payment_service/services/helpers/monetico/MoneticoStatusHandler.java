package com.bookNDrive.payment_service.services.helpers.monetico;

import com.bookNDrive.payment_service.configuration.MoneticoProperties;
import com.bookNDrive.payment_service.entities.Payment;
import com.bookNDrive.payment_service.enums.PaymentStatus;
import com.bookNDrive.payment_service.events.PaymentCreated;
import com.bookNDrive.payment_service.exceptions.InvalidPaymentCallbackException;
import com.bookNDrive.payment_service.exceptions.PaymentNotFoundException;
import com.bookNDrive.payment_service.mappers.PaymentMapper;
import com.bookNDrive.payment_service.repositories.PaymentRepository;
import com.bookNDrive.payment_service.services.OutboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MoneticoStatusHandler {

    private final PaymentRepository paymentRepository;
    private final MoneticoFormBuilder moneticoBuilder;
    private final MoneticoProperties moneticoProperties;
    private final OutboxService outboxService;
    private final PaymentMapper paymentMapper;

    @Transactional
    public String paymentStatus(Map<String, String> returnParameters) throws JsonProcessingException {
        String macRecu = requireParam(returnParameters, "MAC");
        String reference = requireParam(returnParameters, "reference");

        String dataToValidate = moneticoBuilder.dataConstructFromMoneticoReturn(returnParameters);
        String macCalcul = moneticoBuilder.generateMac(dataToValidate, moneticoProperties.key());

        if (macCalcul.equalsIgnoreCase(macRecu)) {
            if ("paiement".equals(returnParameters.get("code-retour")) || "payetest".equals(returnParameters.get("code-retour"))) {
                var payment = markAsSuccess(reference, macRecu, returnParameters);
                outboxService.saveEventBeforePublishing(
                        new PaymentCreated(paymentMapper.paymentToPaymentDto(payment))
                );
            } else {
                markAsFailed(reference, returnParameters);
            }
            return "version=2\ncdr=0\n";
        }

        markAsInvalidSignature(reference, macRecu, returnParameters);
        return "version=2\ncdr=1\n";
    }

    private Payment markAsSuccess(String reference, String macRecu, Map<String, String> retourParams) {
        return paymentRepository.findByReference(reference)
                .map(payment -> {
                    payment.setMacRecu(macRecu);
                    payment.setRawReturnParams(retourParams.toString());
                    payment.setDateValidation(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Europe/Paris")));
                    payment.setStatus(PaymentStatus.SUCCESS);
                    return paymentRepository.save(payment);
                })
                .orElseThrow(() -> new PaymentNotFoundException(reference));
    }

    private void markAsInvalidSignature(String reference, String macRecu, Map<String, String> retourParams) {
        paymentRepository.findByReference(reference).ifPresent(payment -> {
            payment.setMacRecu(macRecu);
            payment.setRawReturnParams(retourParams.toString());
            payment.setStatus(PaymentStatus.INVALID_SIGNATURE);
            paymentRepository.save(payment);
        });
    }

    private void markAsFailed(String reference, Map<String, String> retourParams) {
        paymentRepository.findByReference(reference).ifPresent(payment -> {
            payment.setRawReturnParams(retourParams.toString());
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        });
    }

    private String requireParam(Map<String, String> returnParameters, String key) {
        String value = returnParameters.get(key);
        if (value == null || value.isBlank()) {
            throw new InvalidPaymentCallbackException("Le parametre '" + key + "' est obligatoire");
        }
        return value;
    }
}
