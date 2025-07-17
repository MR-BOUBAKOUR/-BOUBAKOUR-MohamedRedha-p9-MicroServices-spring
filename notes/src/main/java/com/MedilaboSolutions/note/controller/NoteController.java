package com.MedilaboSolutions.note.controller;

import com.MedilaboSolutions.note.dto.NoteRequestDto;
import com.MedilaboSolutions.note.dto.NoteDto;
import com.MedilaboSolutions.note.dto.SuccessResponse;
import com.MedilaboSolutions.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notes")
@RestController
public class NoteController {

    private final NoteService noteService;

    @GetMapping("/{patId}")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getNoteByPatientId(
            @PathVariable Long patId,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId
    ) {
        log.debug("Fetching notes for patientId={}", patId);
        List<NoteDto> notes = noteService.findByPatientId(patId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Note fetched successfully", notes));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<NoteDto>> createNote(@Valid @RequestBody NoteRequestDto noteDto) {
        log.debug("Creating note for patientId={}", noteDto.getPatId());
        NoteDto note = noteService.create(noteDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(201, "Note created successfully", note));
    }

    @DeleteMapping("/{patId}")
    public ResponseEntity<Void> deleteNotesByPatientId(@PathVariable Long patId) {
        log.debug("Deleting notes for patientId={}", patId);
        noteService.deleteByPatientId(patId);

        return ResponseEntity.noContent().build();
    }

}
