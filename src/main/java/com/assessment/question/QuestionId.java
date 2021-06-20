package com.assessment.question;

import lombok.Data;

import java.io.Serializable;

import com.assessment.iam.entities.TenantEntity;

@Data
public class QuestionId extends TenantEntity implements Serializable {
    private String questionId;
}
