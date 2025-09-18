package com.MedilaboSolutions.note.controller;

import com.MedilaboSolutions.note.dto.NoteRequestDto;
import com.MedilaboSolutions.note.dto.NoteDto;
import com.MedilaboSolutions.note.dto.SuccessResponse;
import com.MedilaboSolutions.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<SuccessResponse<Page<NoteDto>>> getNotesByPatientId(
            @PathVariable Long patId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NoteDto> notes = noteService.findByPatientId(patId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Notes fetched successfully", notes));
    }

    @GetMapping("/all/{patId}")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getAllNotesByPatientId(
            @PathVariable Long patId,
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId
    ) {
        log.info("Fetching notes for patientId={}", patId);
        List<NoteDto> notes = noteService.findAllByPatientId(patId);

        return ResponseEntity.ok(new SuccessResponse<>(200, "Note fetched successfully", notes));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<NoteDto>> createNote(@Valid @RequestBody NoteRequestDto noteDto) {
        log.info("Creating note for patientId={}", noteDto.getPatId());
        NoteDto note = noteService.create(noteDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(201, "Note created successfully", note));
    }

    @DeleteMapping("/{patId}")
    public ResponseEntity<Void> deleteNotesByPatientId(@PathVariable Long patId) {
        log.info("Deleting notes for patientId={}", patId);
        noteService.deleteByPatientId(patId);

        return ResponseEntity.noContent().build();
    }

}
