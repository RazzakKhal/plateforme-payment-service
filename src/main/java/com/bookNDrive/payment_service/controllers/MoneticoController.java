package com.bookNDrive.payment_service.controllers;

import com.bookNDrive.payment_service.dtos.sended.PaymentDto;
import com.bookNDrive.payment_service.dtos.sended.PaymentFormDto;
import com.bookNDrive.payment_service.services.MoneticoPaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@Tag(name = "Monetico Controller", description = "Expose les operations de creation et de confirmation des paiements Monetico.")
@Validated
public class MoneticoController {

    private final MoneticoPaymentService paymentService;

    @Autowired
    MoneticoController(MoneticoPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
            summary = "Initialiser un paiement Monetico",
            description = "Genere un formulaire de paiement signe pour la formule demandee a partir de l'utilisateur authentifie."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Formulaire Monetico genere avec succes",
                    content = @Content(schema = @Schema(implementation = PaymentFormDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identifiant de formule invalide ou donnees insuffisantes pour generer le paiement",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentification requise",
                    content = @Content
            )
    })
    @PostMapping("")
    public PaymentFormDto generatePaymentForm(
            @Parameter(
                    description = "Identifiant de la formule a payer",
                    example = "4c3495fd-46d1-4409-bd22-976d9205f6b8",
                    required = true
            )
            @RequestParam @NotNull(message = "must not be null") UUID formulaId
    ) {
        return paymentService.createPayment(formulaId);
    }

    @Operation(
            summary = "Traiter le callback Monetico",
            description = "Recoit les parametres HTTP de retour envoyes par Monetico, verifie la MAC, met a jour le paiement et retourne l'accuse attendu par Monetico."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Callback traite et accuse Monetico retourne",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametres de callback invalides ou incomplets",
                    content = @Content
            )
    })
    @PostMapping("/monetico/callback")
    public String handlePaymentReturn(
            @Parameter(hidden = true)
            @RequestParam Map<String, String> params
    ) throws JsonProcessingException {
        return paymentService.paymentStatus(params);
    }

    @Operation(
            summary = "Récupère un paiement depuis sa référence",
            description = "Récupère un paiement depuis sa référence"
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "Paiement récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = PaymentDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Paiement non trouvé en bdd",
                    content = @Content
            )
    }
    )
    @GetMapping("/{reference}")
    public ResponseEntity<PaymentDto> getPaymentByReference(
            @PathVariable String reference
    ) {
        return ResponseEntity.ok(paymentService.getPaymentByReference(reference));
    }
}
