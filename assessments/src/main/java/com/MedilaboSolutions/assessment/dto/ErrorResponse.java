package com.MedilaboSolutions.assessment.dto;

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
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.message = message;
    }

    private String timestamp;
    private int status;
    private String message;

}
