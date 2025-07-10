package com.MedilaboSolutions.note.mapper;

import com.MedilaboSolutions.note.model.Note;
import com.MedilaboSolutions.note.dto.NoteRequestDto;
import com.MedilaboSolutions.note.dto.NoteDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    NoteDto toNoteDto(Note note);
    Note toNote(NoteDto noteDto);
    Note toNote(NoteRequestDto noteRequestDto);
}
