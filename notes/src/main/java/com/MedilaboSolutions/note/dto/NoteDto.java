package com.MedilaboSolutions.note.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class NoteDto {

    private String id;
    private Long patId;
    private String patFirstName;
    private String note;

    private Instant createdAt;
    private Instant updatedAt;
}
