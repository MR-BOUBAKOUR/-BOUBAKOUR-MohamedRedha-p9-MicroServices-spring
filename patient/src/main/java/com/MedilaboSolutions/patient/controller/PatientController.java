package com.MedilaboSolutions.patient.controller;

import com.MedilaboSolutions.patient.dto.PatientCreateDto;
import com.MedilaboSolutions.patient.dto.PatientDto;
import com.MedilaboSolutions.patient.dto.ResponseDto;
import com.MedilaboSolutions.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/patients")
@RestController
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<ResponseDto<List<PatientDto>>> getAllPatients() {
        List<PatientDto> patients = patientService.findAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto<>(200, "Patients fetched successfully", patients));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<PatientDto>> getPatientById(@PathVariable Long id) {
        PatientDto patient = patientService.findById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto<>(200, "Patient fetched successfully", patient));
    }

    @PostMapping
    public ResponseEntity<ResponseDto<PatientDto>> createPatient(@Valid @RequestBody PatientCreateDto patientDto) {
        PatientDto patient = patientService.create(patientDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto<>(201, "Patient created successfully", patient));
    }
}
