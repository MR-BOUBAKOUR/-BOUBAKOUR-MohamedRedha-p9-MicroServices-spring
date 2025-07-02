package com.MedilaboSolutions.assessment;

import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import com.MedilaboSolutions.assessment.dto.NoteDto;
import com.MedilaboSolutions.assessment.dto.PatientDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import com.MedilaboSolutions.assessment.service.AssessmentService;
import com.MedilaboSolutions.assessment.service.client.NoteFeignClient;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private PatientFeignClient patientFeignClient;

    @Mock
    private NoteFeignClient noteFeignClient;

    @InjectMocks
    private AssessmentService assessmentService;

    @Test
    @DisplayName("Should return 'None' when no trigger terms are found")
    void generateAssessment_ShouldReturnNone_WhenNoTriggers() {
        PatientDto patientDto = new PatientDto();
        patientDto.setGender("M");
        patientDto.setBirthDate(LocalDate.now().minusYears(40));
        SuccessResponse<PatientDto> patientResponse = new SuccessResponse<>(200, "ok", patientDto);

        when(patientFeignClient.getPatientById(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(patientResponse));

        List<NoteDto> notes = List.of();
        SuccessResponse<List<NoteDto>> notesResponse = new SuccessResponse<>(200, "ok", notes);

        when(noteFeignClient.getNoteByPatientId(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(notesResponse));

        AssessmentDto result = assessmentService.generateAssessment(1L, "corrId");

        assertThat(result.getAssessmentResult()).isEqualTo("None");
    }

    @Test
    @DisplayName("Should return 'Borderline' for a man when age > 30 and 2-5 triggers are found")
    void generateAssessment_ShouldReturnBorderline_WhenAgeAbove30_AndTriggersBetween2And5() {
        PatientDto patientDto = new PatientDto();
        patientDto.setGender("M");
        patientDto.setBirthDate(LocalDate.now().minusYears(40));
        SuccessResponse<PatientDto> patientResponse = new SuccessResponse<>(200, "ok", patientDto);

        when(patientFeignClient.getPatientById(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(patientResponse));

        List<NoteDto> notes = List.of(
                new NoteDto(null, 1L, "John", "Fumeur"),
                new NoteDto(null, 1L, "John", "Poids élevé")
        );
        SuccessResponse<List<NoteDto>> notesResponse = new SuccessResponse<>(200, "ok", notes);

        when(noteFeignClient.getNoteByPatientId(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(notesResponse));

        AssessmentDto result = assessmentService.generateAssessment(1L, "corrId");

        assertThat(result.getAssessmentResult()).isEqualTo("Borderline");
    }

    @Test
    @DisplayName("Should return 'In Danger' for a man when age > 30 and 6-7 triggers are found")
    void generateAssessment_ShouldReturnInDanger_WhenAgeAbove30_AndTriggersBetween6And7() {
        PatientDto patientDto = new PatientDto();
        patientDto.setGender("M");
        patientDto.setBirthDate(LocalDate.now().minusYears(40));
        SuccessResponse<PatientDto> patientResponse = new SuccessResponse<>(200, "ok", patientDto);

        when(patientFeignClient.getPatientById(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(patientResponse));

        List<NoteDto> notes = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            notes.add(new NoteDto(null, 1L, "John", "Fumeur"));
        }
        SuccessResponse<List<NoteDto>> notesResponse = new SuccessResponse<>(200, "ok", notes);

        when(noteFeignClient.getNoteByPatientId(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(notesResponse));

        AssessmentDto result = assessmentService.generateAssessment(1L, "corrId");

        assertThat(result.getAssessmentResult()).isEqualTo("In Danger");
    }

    @Test
    @DisplayName("Should return 'Early onset' for a man when age > 30 and 8 or more triggers are found")
    void generateAssessment_ShouldReturnEarlyOnset_WhenAgeAbove30_AndTriggersMoreThen8() {
        PatientDto patientDto = new PatientDto();
        patientDto.setGender("M");
        patientDto.setBirthDate(LocalDate.now().minusYears(40));
        SuccessResponse<PatientDto> patientResponse = new SuccessResponse<>(200, "ok", patientDto);

        when(patientFeignClient.getPatientById(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(patientResponse));

        List<NoteDto> notes = new java.util.ArrayList<>();
        for (int i = 0; i < 8; i++) {
            notes.add(new NoteDto(null, 1L, "John", "Hémoglobine A1C"));
        }
        SuccessResponse<List<NoteDto>> notesResponse = new SuccessResponse<>(200, "ok", notes);

        when(noteFeignClient.getNoteByPatientId(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(notesResponse));

        AssessmentDto result = assessmentService.generateAssessment(1L, "corrId");

        assertThat(result.getAssessmentResult()).isEqualTo("Early onset");
    }
}
