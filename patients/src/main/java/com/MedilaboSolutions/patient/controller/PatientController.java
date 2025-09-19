package com.MedilaboSolutions.patient.controller;

import com.MedilaboSolutions.patient.dto.PatientRequestDto;
import com.MedilaboSolutions.patient.dto.PatientDto;
import com.MedilaboSolutions.patient.dto.SuccessResponse;
import com.MedilaboSolutions.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<SuccessResponse<Page<PatientDto>>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<PatientDto> patients = patientService.findAll(pageable);

        return ResponseEntity.ok(new SuccessResponse<>(200, "Patients fetched successfully", patients));
    }


    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<PatientDto>> getPatientById(
            @PathVariable Long id,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId
    ) {
        log.info("Fetching patient by id={}", id);

        PatientDto patient = patientService.findById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Patient fetched successfully", patient));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<PatientDto>> createPatient(@Valid @RequestBody PatientRequestDto patientDto) {
        log.info("Creating patient for firstName={}", patientDto.getFirstName());

        PatientDto patient = patientService.create(patientDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(201, "Patient created successfully", patient));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<PatientDto>> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequestDto patientDto) {
        log.info("Updating patient id={}", id);

        PatientDto patient = patientService.update(id, patientDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Patient updated successfully", patient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        log.info("Deleting patient id={}", id);

        patientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/early-onset-mail")
    public ResponseEntity<Void> updateEarlyOnsetMailSent(
            @PathVariable Long id,
            @RequestParam Boolean mailSent,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId
    ) {
        log.info("Updating early-onset-mail sent status for patient id={}", id);

        patientService.updateEarlyOnsetMailSent(id, mailSent);
        return ResponseEntity.noContent().build();
    }
}
