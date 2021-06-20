package com.assessment.questionpaper.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SaveAnswerRequestDto {

    @NotNull
    @NotEmpty
    private String assignmentId;

    @NotNull
    @NotEmpty
    private String sectionId;

    @NotNull
    @NotEmpty
    private String questionId;

    private String answerText;
    private List<String> selectedOptions;

    private int timeElapsedInSec;
    private boolean markForReview;
}
