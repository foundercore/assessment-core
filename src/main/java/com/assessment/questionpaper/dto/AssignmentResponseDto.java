package com.assessment.questionpaper.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AssignmentResponseDto {

    private String assignmentId;
    private String testId;
    private String testName;
//    private List<String> assignedToEntity;
//    private String entityType;
    private List<String> assignedToBatch;
    private List<String> assignedToStudent;
    private boolean attempted;  // user specific. reference test_submission
    private double marksReceived;  // user specific. reference test_submission only if evaluation completed
    private double totalMarks;  // user specific. reference test_submission only if evaluation completed

    private String passcode;
    private String description;

    private Date releaseDate;
    private Date validFrom;
    private Date validTo;

    private String scoringType;
    private List<String> absentUsers;
    private List<String> tags;

    /* activity logs */
    private String createdBy;
    private Date createdOn;
    private String lastUpdatedBy;
    private Date lastUpdatedOn;
}
