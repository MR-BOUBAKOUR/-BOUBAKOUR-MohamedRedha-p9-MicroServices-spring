package com.MedilaboSolutions.note.unit;

import com.MedilaboSolutions.note.controller.NoteController;
import com.MedilaboSolutions.note.dto.NoteDto;
import com.MedilaboSolutions.note.dto.NoteRequestDto;
import com.MedilaboSolutions.note.service.NoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @Autowired
    private ObjectMapper objectMapper;

    private NoteDto noteDto;
    private NoteRequestDto noteRequestDto;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Should return notes when patient ID exists")
    void getNoteByPatientId_ShouldReturnNotes() throws Exception {
        when(noteService.findByPatientId(1L)).thenReturn(List.of(noteDto));

        mockMvc.perform(get("/notes/1")
                        .header("medilabo-solutions-correlation-id", "test-correlation-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Note fetched successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("abc123"));

        verify(noteService).findByPatientId(1L);
    }

    @Test
    @DisplayName("Should create note when input data is valid")
    void createNote_WithValidData_ShouldCreateNote() throws Exception {
        when(noteService.create(any(NoteRequestDto.class))).thenReturn(noteDto);

        mockMvc.perform(post("/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Note created successfully"))
                .andExpect(jsonPath("$.data.id").value("abc123"));

        verify(noteService).create(any(NoteRequestDto.class));
    }

    @Test
    @DisplayName("Should delete notes when patient ID exists")
    void deleteNotesByPatientId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/notes/1"))
                .andExpect(status().isNoContent());

        verify(noteService).deleteByPatientId(1L);
    }
}

