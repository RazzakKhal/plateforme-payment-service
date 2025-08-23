package com.bookNDrive.payment_service.controllers;

import com.bookNDrive.payment_service.dtos.sended.PaymentFormDto;
import com.bookNDrive.payment_service.services.MoneticoPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class MoneticoController {



    private final MoneticoPaymentService paymentService;



    @Autowired
    MoneticoController(MoneticoPaymentService paymentService){
        this.paymentService = paymentService;

    }

    @PostMapping("/initier")
    public PaymentFormDto generatePaymentForm(@RequestParam String formulaId) {

        return paymentService.createPayment(formulaId);
    }


    @PostMapping("/retour")
    public String handlePaymentReturn(@RequestParam Map<String, String> params) {

        return paymentService.paymentStatus(params);
    }
}
