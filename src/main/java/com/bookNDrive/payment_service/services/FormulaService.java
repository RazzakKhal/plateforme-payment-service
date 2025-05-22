package com.bookNDrive.payment_service.services;

import com.bookNDrive.payment_service.feign.formula_service.FormulaServiceFeignClient;
import com.bookNDrive.payment_service.feign.formula_service.dtos.FormulaDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class FormulaService {

    private final FormulaServiceFeignClient formulaServiceFeignClient;

    public FormulaService(FormulaServiceFeignClient formulaServiceFeignClient){
        this.formulaServiceFeignClient = formulaServiceFeignClient;
    }

    public FormulaDto getFormulaById(Long id){
        var token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        return formulaServiceFeignClient.getFormula(id,"Bearer " +token).getBody();
    }

    public String getPrice(FormulaDto formula){
        return String.valueOf(formula.getPromotionnalPrice() != null ? formula.getPromotionnalPrice() : formula.getPrice());
    }
}
