package com.MedilaboSolutions.note.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NoteRequestDto {

    private Long patId;
    private String patFirstName;

    @NotBlank(message = "Note content must not be blank.")
    private String note;

}
