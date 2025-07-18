package com.MedilaboSolutions.assessment.service.client;

import com.MedilaboSolutions.assessment.dto.PatientDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient("patients")
public interface PatientFeignClient {

    @GetMapping(value = "/patients/{id}")
    ResponseEntity<SuccessResponse<PatientDto>> getPatientById(
            @PathVariable Long id,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId
    );

    @PutMapping(value = "/patients/{id}/early-onset-mail")
    void updateEarlyOnsetMailSent(
            @PathVariable Long id,
            @RequestParam boolean mailSent,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId);

}