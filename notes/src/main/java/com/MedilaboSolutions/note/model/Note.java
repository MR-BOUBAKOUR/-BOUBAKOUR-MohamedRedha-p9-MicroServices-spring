package com.MedilaboSolutions.note.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "notes")
public class Note {

    @Id
    private String id;

    private Long patId;
    private String patFirstName;
    private String note;
}
