package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.enums.PaymentStatus;
import com.bookNDrive.payment_service.exceptions.PaymentNotFoundException;
import com.bookNDrive.payment_service.repositories.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletionService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void completePayment(String reference) {
        var payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new PaymentNotFoundException(reference));

        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
        log.info("Paiement complete reference={} status={}", reference, PaymentStatus.COMPLETED);
    }
}
