package com.MedilaboSolutions.assessment.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NoteDto {

    private String id;
    private Long patId;
    private String patFirstName;
    private String note;

}
