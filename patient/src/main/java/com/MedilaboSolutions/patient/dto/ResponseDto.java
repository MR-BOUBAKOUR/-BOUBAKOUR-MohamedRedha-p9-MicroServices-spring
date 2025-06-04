package com.MedilaboSolutions.patient.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
public class ResponseDto<T> {

    public ResponseDto(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().toString();
    }

    private int status;
    private String message;
    private T data;
    private String timestamp;

}
