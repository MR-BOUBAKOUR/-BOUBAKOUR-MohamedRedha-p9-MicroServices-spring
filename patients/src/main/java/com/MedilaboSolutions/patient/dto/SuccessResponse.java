package com.MedilaboSolutions.patient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter @Setter
@ToString
@NoArgsConstructor
public class SuccessResponse<T> {

    public SuccessResponse(int status, String message, T data) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    private String timestamp;
    private int status;
    private String message;
    private T data;

}
