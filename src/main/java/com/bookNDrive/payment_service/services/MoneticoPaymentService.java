package com.bookNDrive.payment_service.services;


import com.bookNDrive.payment_service.dtos.sended.PaymentFormDto;
import com.bookNDrive.payment_service.models.Payment;
import com.bookNDrive.payment_service.repositories.PaymentRepository;
import com.bookNDrive.payment_service.services.helpers.monetico.MoneticoFormBuilder;
import com.bookNDrive.payment_service.services.helpers.monetico.MoneticoStatusHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MoneticoPaymentService implements PaymentService{



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

            ){
        this.userService =userService;
        this.formulaService = formulaService;
        this.moneticoStatusHandler = moneticoStatusHandler;
        this.moneticoBuilder = moneticoBuilder;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentFormDto createPayment(String formulaId) {

        var user = userService.getCurrentUser();
        var formula = formulaService.getFormulaById(Long.valueOf(formulaId));
        var price = formulaService.getPrice(formula);

        PaymentFormDto paymentFormDto = moneticoBuilder.build(user,price);

        Payment payment = new Payment();
        payment.setReference(paymentFormDto.reference());
        payment.setUserId(user.getId());
        payment.setFormulaId(Long.valueOf(formulaId));
        payment.setMontant(price);
        payment.setMacEnvoye(paymentFormDto.MAC());
        payment.setContexteCommande(paymentFormDto.contexte_commande());

        paymentRepository.save(payment);

        return paymentFormDto;
    }



    public String paymentStatus(Map<String, String> returnParameters){
        return moneticoStatusHandler.paymentStatus(returnParameters);
    }


}
