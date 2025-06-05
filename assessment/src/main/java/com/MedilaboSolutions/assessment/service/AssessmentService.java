package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import com.MedilaboSolutions.assessment.dto.NoteDto;
import com.MedilaboSolutions.assessment.dto.PatientDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import com.MedilaboSolutions.assessment.service.client.NoteFeignClient;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AssessmentService {

    private final PatientFeignClient patientFeignClient;
    private final NoteFeignClient noteFeignClient;

    public AssessmentDto generateAssessment(Long patId) {

        ResponseEntity<SuccessResponse<PatientDto>> patient = patientFeignClient.getPatientById(patId);
        ResponseEntity<SuccessResponse<List<NoteDto>>> notes = noteFeignClient.getNoteByPatientId(patId);

        log.info("Patient found: {}", patient.getBody().getData());
        log.info("Notes found: {}", notes.getBody().getData());

        return null;
    }

}
