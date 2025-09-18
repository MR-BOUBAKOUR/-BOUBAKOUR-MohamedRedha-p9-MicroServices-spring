package com.MedilaboSolutions.note.unit;

import com.MedilaboSolutions.note.model.Note;
import com.MedilaboSolutions.note.dto.NoteRequestDto;
import com.MedilaboSolutions.note.dto.NoteDto;
import com.MedilaboSolutions.note.mapper.NoteMapper;
import com.MedilaboSolutions.note.repository.NoteRepository;
import com.MedilaboSolutions.note.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private NoteService noteService;

    private Note note;
    private NoteDto noteDto;
    private NoteRequestDto noteRequestDto;

    @BeforeEach
    void setUp() {
        note = new Note();
        note.setId("abc123");
        note.setPatId(1L);
        note.setPatFirstName("TestFirstName");
        note.setNote("Test note content");

        noteDto = new NoteDto();
        noteDto.setId("abc123");
        noteDto.setPatId(1L);
        noteDto.setPatFirstName("TestFirstName");
        noteDto.setNote("Test note content");

        noteRequestDto = new NoteRequestDto();
        noteRequestDto.setPatId(1L);
        noteRequestDto.setPatFirstName("TestFirstName");
        noteRequestDto.setNote("Test note content");
    }

    @Test
    @DisplayName("Should return page of notes when patient ID exists")
    void findByPatientId_ShouldReturnNotesPage() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Note> notePage = new PageImpl<>(List.of(note));
        when(noteRepository.findByPatId(1L, pageable)).thenReturn(notePage);
        when(noteMapper.toNoteDto(note)).thenReturn(noteDto);

        Page<NoteDto> result = noteService.findByPatientId(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(noteDto);

        verify(noteRepository).findByPatId(1L, pageable);
        verify(noteMapper).toNoteDto(note);
    }


    @Test
    @DisplayName("Should save and return note when input is valid")
    void create_ShouldSaveAndReturnNote() {
        when(noteMapper.toNote(noteRequestDto)).thenReturn(note);
        when(noteRepository.save(note)).thenReturn(note);
        when(noteMapper.toNoteDto(note)).thenReturn(noteDto);

        NoteDto result = noteService.create(noteRequestDto);

        assertThat(result).isEqualTo(noteDto);

        verify(noteMapper).toNote(noteRequestDto);
        verify(noteRepository).save(note);
        verify(noteMapper).toNoteDto(note);
    }

    @Test
    @DisplayName("Should delete notes when patient ID exists")
    void deleteByPatientId_ShouldDeleteNotes() {
        when(noteRepository.findByPatId(1L)).thenReturn(List.of(note));

        noteService.deleteByPatientId(1L);

        verify(noteRepository).findByPatId(1L);
        verify(noteRepository).deleteAll(List.of(note));
    }

    @Test
    @DisplayName("Should not delete when no notes found")
    void deleteByPatientId_ShouldDoNothingWhenNoNotesFound() {
        when(noteRepository.findByPatId(1L)).thenReturn(List.of());

        noteService.deleteByPatientId(1L);

        verify(noteRepository).findByPatId(1L);
        verify(noteRepository, never()).deleteAll(any());
    }
}

