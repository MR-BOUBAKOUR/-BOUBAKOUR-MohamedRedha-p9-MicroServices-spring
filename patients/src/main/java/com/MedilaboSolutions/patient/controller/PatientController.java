package com.MedilaboSolutions.patient.controller;

import com.MedilaboSolutions.patient.dto.PatientRequestDto;
import com.MedilaboSolutions.patient.dto.PatientDto;
import com.MedilaboSolutions.patient.dto.SuccessResponse;
import com.MedilaboSolutions.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/patients")
@RestController
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<PatientDto>>> getAllPatients() {
        List<PatientDto> patients = patientService.findAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Patients fetched successfully", patients));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<PatientDto>> getPatientById(
            @PathVariable Long id,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId
    ) {
        log.debug("medilabo-solutions-correlation-id found : {}", correlationId);

        PatientDto patient = patientService.findById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Patient fetched successfully", patient));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<PatientDto>> createPatient(@Valid @RequestBody PatientRequestDto patientDto) {
        PatientDto patient = patientService.create(patientDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(201, "Patient created successfully", patient));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<PatientDto>> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequestDto patientDto) {
        PatientDto patient = patientService.update(id, patientDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Patient updated successfully", patient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/early-onset-mail")
    public ResponseEntity<Void> updateEarlyOnsetMailSent(
            @PathVariable Long id,
            @RequestParam Boolean mailSent,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId
    ) {
        log.debug("medilabo-solutions-correlation-id found : {}", correlationId);

        patientService.updateEarlyOnsetMailSent(id, mailSent);
        return ResponseEntity.noContent().build();
    }
}
