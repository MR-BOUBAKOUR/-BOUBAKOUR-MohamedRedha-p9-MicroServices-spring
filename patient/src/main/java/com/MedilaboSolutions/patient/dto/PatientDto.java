package com.MedilaboSolutions.patient.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PatientDto {

    private Long id;

    @NotBlank
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotBlank
    @Pattern(regexp = "^[MF]$", message = "Gender must be 'M' or 'F'")
    private String gender;

    @Size(max = 100)
    private String address;

    @Size(max = 20)
    @Pattern(regexp = "^\\d{3}-\\d{3}-\\d{4}$", message = "Phone number must match XXX-XXX-XXXX format")
    private String phone;
}
