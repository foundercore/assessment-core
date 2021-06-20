package com.assessment.questionpaper.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AssignmentRequestDto {

    @NotNull
    @NotEmpty
    private String testId;

    private List<String> assignedToBatch;
    private List<String> assignedToStudent;

    private String passcode;

    private String description;

    private String releaseDate;  // fixed   format - yyyy-mm-dd hh:mm:ss
    private String validFrom;     // range based if assignment can be given any time b/w validFrom & validTo, format - yyyy-mm-dd hh:mm:ss
    private String validTo;       // range based if assignment can be given any time b/w validFrom & validTo, format - yyyy-mm-dd hh:mm:ss

    private String scoringType;     // possible values ?

    private List<String> tags;
}
