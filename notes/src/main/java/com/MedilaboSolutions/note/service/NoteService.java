package com.MedilaboSolutions.note.service;

import com.MedilaboSolutions.note.model.Note;
import com.MedilaboSolutions.note.dto.NoteRequestDto;
import com.MedilaboSolutions.note.dto.NoteDto;
import com.MedilaboSolutions.note.mapper.NoteMapper;
import com.MedilaboSolutions.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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

    public void deleteByPatientId(Long patId) {
        List<Note> notes = noteRepository.findByPatId(patId);
        if (!notes.isEmpty()) {
            noteRepository.deleteAll(notes);
        }
    }
}
