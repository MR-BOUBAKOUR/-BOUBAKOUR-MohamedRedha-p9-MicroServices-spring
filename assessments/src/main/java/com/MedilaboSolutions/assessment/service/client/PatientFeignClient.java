package com.MedilaboSolutions.assessment.service.client;

import com.MedilaboSolutions.assessment.dto.PatientDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("patients")
public interface PatientFeignClient {

    @GetMapping(value = "/patients/{id}")
    ResponseEntity<SuccessResponse<PatientDto>> getPatientById(
            @PathVariable Long id,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId
    );

}
