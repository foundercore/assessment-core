package com.assessment.questionpaper.entity;

import lombok.Data;

import java.io.Serializable;

import com.assessment.iam.entities.TenantEntity;

@Data
public class AssignmentId extends TenantEntity implements Serializable {
    private String assignmentId;
}
