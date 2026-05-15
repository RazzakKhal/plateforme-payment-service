package com.bookNDrive.payment_service.feign.formula_service;

import com.bookNDrive.payment_service.feign.formula_service.dtos.FormulaDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient("formula-service")
public interface FormulaServiceFeignClient {

    @GetMapping("/formulas/{id}")
    public ResponseEntity<FormulaDto> getFormula(@PathVariable UUID id, @RequestHeader("Authorization") String bearerToken);
}
