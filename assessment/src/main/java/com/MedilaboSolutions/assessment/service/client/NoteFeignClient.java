package com.MedilaboSolutions.assessment.service.client;

import com.MedilaboSolutions.assessment.dto.NoteDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("note")
public interface NoteFeignClient {

    @GetMapping(value = "/notes/{patId}", consumes = "application/json")
    ResponseEntity<SuccessResponse<List<NoteDto>>> getNoteByPatientId(@PathVariable Long patId);

}
