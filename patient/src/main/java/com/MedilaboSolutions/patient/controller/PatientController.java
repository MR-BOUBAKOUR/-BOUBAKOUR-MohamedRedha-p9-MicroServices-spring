package com.MedilaboSolutions.patient.controller;

import com.MedilaboSolutions.patient.dto.PatientCreateDto;
import com.MedilaboSolutions.patient.dto.PatientDto;
import com.MedilaboSolutions.patient.dto.ResponseDto;
import com.MedilaboSolutions.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/patients")
    public ResponseEntity<ResponseDto<List<PatientDto>>> getAllPatients() {
        List<PatientDto> patients = patientService.findAll();
        ResponseDto<List<PatientDto>> response = new ResponseDto<>(200, "Patients fetched successfully", patients);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patients/{id}")
    public ResponseEntity<ResponseDto<PatientDto>> getPatientById(@PathVariable Long id) {
        PatientDto patient = patientService.findById(id);
        ResponseDto<PatientDto> response = new ResponseDto<>(200, "Patient fetched successfully", patient);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patients/create")
    public ResponseEntity<ResponseDto<PatientDto>> createPatient(@RequestBody PatientCreateDto patientDto) {
        PatientDto patient = patientService.create(patientDto);
        ResponseDto<PatientDto> response = new ResponseDto<>(200, "Patient created successfully", patient);
        return ResponseEntity.ok(response);
    }
}
