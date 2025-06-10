package com.MedilaboSolutions.note.service;

import com.MedilaboSolutions.note.domain.Note;
import com.MedilaboSolutions.note.dto.NoteRequestDto;
import com.MedilaboSolutions.note.dto.NoteDto;
import com.MedilaboSolutions.note.mapper.NoteMapper;
import com.MedilaboSolutions.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    public List<NoteDto> findByPatientId(Long patId) {
        List<Note> notes = noteRepository.findByPatId(patId);

        return notes.stream()
                .map(noteMapper::toNoteDto)
                .toList();
    }

    public NoteDto create(NoteRequestDto noteDto) {
        Note saved = noteRepository.save(noteMapper.toNote(noteDto));
        return noteMapper.toNoteDto(saved);
    }

}
