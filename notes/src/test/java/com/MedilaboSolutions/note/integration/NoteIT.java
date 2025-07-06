package com.MedilaboSolutions.note.integration;

import com.MedilaboSolutions.note.config.AbstractMongoContainerTest;
import com.MedilaboSolutions.note.dto.NoteRequestDto;
import com.MedilaboSolutions.note.repository.NoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class NoteIT extends AbstractMongoContainerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return notes when patient ID exists")
    void getNoteByPatientId_ShouldReturnNotes() throws Exception {
        mockMvc.perform(get("/notes/1")
                        .header("medilabo-solutions-correlation-id", "test-correlation-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].patId").value(1));
    }

    @Test
    @DisplayName("Should create note when input data is valid")
    void createNote_WithValidData_ShouldCreateNote() throws Exception {
        NoteRequestDto createRequest = new NoteRequestDto();
        createRequest.setPatId(99L);
        createRequest.setPatFirstName("TestFirstName");
        createRequest.setNote("Test note content");

        String createResponse = mockMvc.perform(post("/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.patId").value(99))
                .andReturn().getResponse().getContentAsString();

        String newNoteId = objectMapper.readTree(createResponse).get("data").get("id").asText();
        assertThat(noteRepository.findById(newNoteId)).isPresent();
    }
}
