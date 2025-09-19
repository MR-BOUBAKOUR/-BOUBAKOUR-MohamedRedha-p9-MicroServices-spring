package com.MedilaboSolutions.note.service;

import com.MedilaboSolutions.note.model.Note;
import com.MedilaboSolutions.note.dto.NoteRequestDto;
import com.MedilaboSolutions.note.dto.NoteDto;
import com.MedilaboSolutions.note.mapper.NoteMapper;
import com.MedilaboSolutions.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    public Page<NoteDto> findByPatientId(Long patId, Pageable pageable) {
        Page<Note> notes = noteRepository.findByPatId(patId, pageable);
        return notes.map(noteMapper::toNoteDto);
    }

    public List<NoteDto> findAllByPatientId(Long patId) {
        return noteRepository.findByPatId(patId)
                .stream()
                .map(noteMapper::toNoteDto)
                .collect(Collectors.toList());
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
