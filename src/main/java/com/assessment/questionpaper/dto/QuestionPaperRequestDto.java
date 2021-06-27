package com.assessment.questionpaper.dto;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class QuestionPaperRequestDto {

    @NotNull
    @NotEmpty
    private String name;

    private String subject;
    private String type;
    private int totalDurationInMinutes;
    private int minimumDurationInMinutes;
    private int totalMarks;
    private String instructions;
    private String status;

    List<PaperSectionRequestDto> sections;
    List<String> sectionOrder;
	private boolean calculatorRequired = true;
    private List<String> tags;

    @Data
    public static class PaperSectionRequestDto {

        private String name;
        private int durationInMinutes;
        private String difficultyLevel;
        private String instructions;
        private List<TestQuestionRequestDto> questions;

        /* to support nested sub-sections */
        private String parentSection;
        List<String> subSectionOrder;
    }

    @Data
    public static class TestQuestionRequestDto {

        private String id;
        private double positiveMark = 0;
        private double negativeMark = 0;
        private double skipMark = 0;
    }
}
