package com.assessment.questionpaper.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.assessment.questionpaper.dto.AssignmentResponseDto;

import lombok.Data;

@Data
@Document(collection = Assignment.COLLECTION_NAME)
public class Assignment {
    public static final String COLLECTION_NAME = "test_assignment";

    @Id
    private AssignmentId id;
    private String testId;
//    private List<String> assignedToEntity;
//    private String entityType;  //batch/individual

    private List<String> assignedToBatch;
    private List<String> assignedToStudent;

    private String passcode;    //system generated
    private String description;

    private Date releaseDate;  // fixed
    private Date validFrom;     // range based if assignment can be given any time b/w validFrom & validTo
    private Date validTo;       // range based if assignment can be given any time b/w validFrom & validTo

    private String scoringType;     // possible values ?
    private List<String> absentUsers;   //who have missed test
    private List<String> tags;
	private String testType;

    /* activity logs */
    private String createdBy;
    private Date createdOn;
    @LastModifiedBy
    private String lastUpdatedBy;
    @LastModifiedDate
    private Date lastUpdatedOn;

    public void addAbsentUsers(String studentId){
        if (this.absentUsers == null) this.absentUsers = new ArrayList<>();
        if (!this.absentUsers.contains(studentId)) this.absentUsers.add(studentId);
    }

    public AssignmentResponseDto toResponseDto(){
        AssignmentResponseDto dto = new AssignmentResponseDto();
        dto.setAssignmentId(this.id.getAssignmentId());
        dto.setTestId(this.testId);
//        dto.setAssignedToEntity(this.assignedToEntity);
//        dto.setEntityType(this.entityType);
        dto.setAssignedToBatch(this.assignedToBatch);
        dto.setAssignedToStudent(this.assignedToStudent);

        dto.setPasscode(this.passcode);
        dto.setDescription(this.description);
        dto.setReleaseDate(this.releaseDate);
        dto.setValidFrom(this.validFrom);
        dto.setValidTo(this.validTo);
        dto.setScoringType(this.scoringType);
        dto.setAbsentUsers(this.absentUsers);
        dto.setTags(this.tags);
		dto.setTestType(this.testType);
        dto.setCreatedOn(this.createdOn);
        dto.setCreatedBy(this.createdBy);
        dto.setLastUpdatedOn(this.lastUpdatedOn);
        dto.setLastUpdatedBy(this.lastUpdatedBy);
        return dto;
    }
}
