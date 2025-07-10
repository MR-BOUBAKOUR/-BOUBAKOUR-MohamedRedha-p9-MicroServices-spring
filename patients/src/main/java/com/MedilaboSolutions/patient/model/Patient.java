package com.MedilaboSolutions.patient.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(length = 1, nullable = false)
    private String gender;

    @Column(length = 100)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(name = "early_onset_mail_sent", nullable = false)
    private boolean earlyOnsetMailSent = false;
}
