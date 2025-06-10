package com.MedilaboSolutions.note.controller;

import com.MedilaboSolutions.note.dto.NoteRequestDto;
import com.MedilaboSolutions.note.dto.NoteDto;
import com.MedilaboSolutions.note.dto.SuccessResponse;
import com.MedilaboSolutions.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/notes")
@RestController
public class NoteController {

    private final NoteService noteService;

    @GetMapping("/{patId}")
    public ResponseEntity<SuccessResponse<List<NoteDto>>> getNoteByPatientId(@PathVariable Long patId) {
        List<NoteDto> notes = noteService.findByPatientId(patId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Note fetched successfully", notes));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<NoteDto>> createNote(@Valid @RequestBody NoteRequestDto noteDto) {
        NoteDto note = noteService.create(noteDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(201, "Note created successfully", note));
    }

}
