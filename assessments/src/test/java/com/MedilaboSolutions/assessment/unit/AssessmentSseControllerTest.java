package com.MedilaboSolutions.assessment.unit;

import com.MedilaboSolutions.assessment.controller.AssessmentSseController;
import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.mockito.Mockito.*;

class AssessmentSseControllerTest {

    private AssessmentSseController sseController;

    @BeforeEach
    void setUp() {
        sseController = new AssessmentSseController();
    }

    @Test
    @DisplayName("Should subscribe patient and store emitter")
    void subscribe_ShouldStoreEmitter() {
        SseEmitter emitter = sseController.subscribe(1L);

        // L'emitter est bien stocké
        assert sseController.getEmitters().containsKey(1L);
        assert sseController.getEmitters().get(1L) == emitter;
    }

    @Test
    @DisplayName("Should emit assessment progress")
    void emitAssessmentProgress_ShouldSendEvent() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        sseController.getEmitters().put(1L, mockEmitter);

        sseController.emitAssessmentProgress(100L, 1L, "In progress", 50);

        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("Should emit generated assessment")
    void emitAssessmentGenerated_ShouldSendEvent() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        sseController.getEmitters().put(1L, mockEmitter);

        // Crée un AssessmentDto factice
        var dto = new AssessmentDto();
        dto.setPatId(1L);

        sseController.emitAssessmentGenerated(dto);

        verify(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }
}
