package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.dtos.sended.PaymentFormDto;
import com.bookNDrive.payment_service.entities.Payment;
import com.bookNDrive.payment_service.enums.PaymentStatus;
import com.bookNDrive.payment_service.repositories.PaymentRepository;
import com.bookNDrive.payment_service.services.helpers.monetico.MoneticoFormBuilder;
import com.bookNDrive.payment_service.services.helpers.monetico.MoneticoStatusHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class MoneticoPaymentService implements PaymentService {

    private final UserService userService;
    private final FormulaService formulaService;
    private final MoneticoStatusHandler moneticoStatusHandler;
    private final MoneticoFormBuilder moneticoBuilder;
    private final PaymentRepository paymentRepository;

    @Autowired
    public MoneticoPaymentService(
            UserService userService,
            FormulaService formulaService,
            MoneticoStatusHandler moneticoStatusHandler,
            MoneticoFormBuilder moneticoBuilder,
            PaymentRepository paymentRepository
    ) {
        this.userService = userService;
        this.formulaService = formulaService;
        this.moneticoStatusHandler = moneticoStatusHandler;
        this.moneticoBuilder = moneticoBuilder;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public PaymentFormDto createPayment(Long formulaId) {
        log.info("Creation de paiement initiee formulaId={}", formulaId);
        var user = userService.getCurrentUser();
        var formula = formulaService.getFormulaById(formulaId);
        var price = formulaService.getPrice(formula);
        log.info("Donnees de paiement resolues formulaId={} userId={} amount={}", formulaId, user.getId(), price);

        PaymentFormDto paymentFormDto = moneticoBuilder.build(user, price);

        Payment payment = new Payment();
        payment.setReference(paymentFormDto.reference());
        payment.setUserId(user.getId());
        payment.setFormulaId(formulaId);
        payment.setMontant(price);
        payment.setMacEnvoye(paymentFormDto.MAC());
        payment.setContexteCommande(paymentFormDto.contexte_commande());
        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);
        log.info(
                "Paiement initialise paymentReference={} userId={} formulaId={} status={}",
                payment.getReference(),
                payment.getUserId(),
                payment.getFormulaId(),
                payment.getStatus()
        );

        return paymentFormDto;
    }

    @Override
    public String paymentStatus(Map<String, String> returnParameters) throws JsonProcessingException {
        return moneticoStatusHandler.paymentStatus(returnParameters);
    }
}
