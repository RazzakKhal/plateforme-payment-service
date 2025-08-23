package com.bookNDrive.payment_service.dtos.sended;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentFormDto(
        String date,
        String mail,
        @JsonProperty("3dsdebrayable") String _3dsdebrayable,
        String MAC,
        String TPE,
        String ThreeDSecureChallenge,
        String contexte_commande,
        String lgue,
        String montant,
        String reference,
        String societe,
        @JsonProperty("texte-libre") String texte_libre,
        String url_retour_err,
        String url_retour_ok,
        String version
) {}
