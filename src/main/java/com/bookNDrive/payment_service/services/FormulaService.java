package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.exceptions.UpstreamServiceException;
import com.bookNDrive.payment_service.feign.formula_service.FormulaServiceFeignClient;
import com.bookNDrive.payment_service.feign.formula_service.dtos.FormulaDto;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FormulaService {

    private final FormulaServiceFeignClient formulaServiceFeignClient;

    public FormulaService(FormulaServiceFeignClient formulaServiceFeignClient) {
        this.formulaServiceFeignClient = formulaServiceFeignClient;
    }

    public FormulaDto getFormulaById(Long id) {
        log.info("Appel au formula-service initie formulaId={}", id);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            log.warn("Appel au formula-service impossible, jeton utilisateur absent formulaId={}", id);
            throw new UpstreamServiceException("Aucun jeton utilisateur n'est disponible pour l'appel au formula-service");
        }

        String token = authentication.getCredentials().toString();

        try {
            var response = formulaServiceFeignClient.getFormula(id, "Bearer " + token);
            if (response.getBody() == null) {
                log.error("Le formula-service a repondu sans corps formulaId={}", id);
                throw new UpstreamServiceException("Le formula-service n'a retourne aucune formule");
            }
            log.info("Appel au formula-service reussi formulaId={}", id);
            return response.getBody();
        } catch (FeignException ex) {
            log.error("Echec de l'appel au formula-service formulaId={}", id, ex);
            throw new UpstreamServiceException("Le formula-service est indisponible ou a refuse la requete");
        }
    }

    public String getPrice(FormulaDto formula) {
        return String.valueOf(formula.getPromotionnalPrice() != null ? formula.getPromotionnalPrice() : formula.getPrice());
    }
}
