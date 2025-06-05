package com.MedilaboSolutions.patient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter @Setter
@ToString
@NoArgsConstructor
public class ErrorResponse {

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }

    private int status;
    private String message;
    private String timestamp;

}
