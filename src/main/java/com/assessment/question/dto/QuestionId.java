package com.assessment.question.dto;

import java.io.Serializable;

import com.assessment.iam.entities.TenantEntity;

import lombok.Data;

@Data
public class QuestionId extends TenantEntity implements Serializable {
	private String questionId;

	public QuestionId() {

	}

	public QuestionId(String questionId, String tenantId) {
		this.questionId = questionId;
		this.tenantId = tenantId;
	}

}
