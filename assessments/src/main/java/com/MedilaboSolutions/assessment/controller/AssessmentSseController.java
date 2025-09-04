package com.MedilaboSolutions.assessment.controller;

import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/assessments/sse")
public class AssessmentSseController {

    // Chaque patient a son SseEmitter
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/{patientId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long patientId) {
        // Timeout infini
        SseEmitter emitter = new SseEmitter(16 * 60_000L); // 16 minutes (the accessToken expires in 15 minutes)
        emitters.put(patientId, emitter);

        // Nettoyage quand le client se déconnecte
        emitter.onCompletion(() -> emitters.remove(patientId));
        emitter.onTimeout(() -> emitters.remove(patientId));
        emitter.onError((e) -> emitters.remove(patientId));

        log.info("Patient {} subscribed to SSE", patientId);
        return emitter;
    }

    public void emitAssessmentProgress(Long assessmentId, Long patId, String message, Integer percent) {
        SseEmitter emitter = emitters.get(patId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("assessment-progress")
                        .data(Map.of(
                                "assessmentId", assessmentId,
                                "patId", patId,
                                "message", message,
                                "progress", percent
                        ))
                );
            } catch (IOException e) {
                log.warn("Failed to send progress SSE to patientId {}: {}", patId, e.getMessage());
                emitters.remove(patId);
            }
        }
    }

    public void emitAssessmentGenerated(AssessmentDto dto) {
        Long patientId = dto.getPatId();
        SseEmitter emitter = emitters.get(patientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("assessment-generated")
                        .data(dto)
                );
            } catch (IOException e) {
                log.warn("Failed to send generated SSE to patientId {}: {}", patientId, e.getMessage());
                emitters.remove(patientId);
            }
        }
    }

    @Scheduled(fixedRate = 30000)
    public void emitHeartbeatToAll() {
        emitters.forEach((patientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .comment("ping")
                );
            } catch (IOException e) {
                log.warn("Heartbeat failed for patientId {}: {}", patientId, e.getMessage());
                emitters.remove(patientId);
            }
        });
    }
}
