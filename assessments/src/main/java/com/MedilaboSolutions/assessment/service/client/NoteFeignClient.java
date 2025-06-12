package com.MedilaboSolutions.assessment.service.client;

import com.MedilaboSolutions.assessment.dto.NoteDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient("notes")
public interface NoteFeignClient {

    @GetMapping(value = "/notes/{patId}")
    ResponseEntity<SuccessResponse<List<NoteDto>>> getNoteByPatientId(
            @PathVariable Long patId,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId
    );

}
