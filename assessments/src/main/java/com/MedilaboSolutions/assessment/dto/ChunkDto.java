package com.MedilaboSolutions.assessment.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChunkDto {
    private String text;
    private Metadata metadata;

    @Data
    public static class Metadata {
        private List<String> titles;
        private List<Integer> pages;
        private List<String> refs;
    }
}
