package com.MedilaboSolutions.assessment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class NoteDto {

    private String id;
    private Long patId;
    private String patFirstName;
    private String note;

}
