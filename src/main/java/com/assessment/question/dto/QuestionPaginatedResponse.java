package com.assessment.question.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.assessment.question.dto.Question.QuestionAnswer;
import com.assessment.question.dto.Question.QuestionOption;

import lombok.Data;

@Data
public class QuestionPaginatedResponse {
	private List<QuestionResponseDto> questions;
	private int pageSize;
	private int pageNumber;
	private long totalRecords;

	@Data
	public static class QuestionResponseDto {
		private QuestionId id;
		private String name;
		private String description;
		private String type;
		private String subject;
		private String topic;
		private String subTopic;
		private String difficultyLevel;
		private String passageId;
		private String passageContent;
		private List<QuestionOption> options;
		private List<String> tags;
		private String reference;
		private String explanation;
		private String videoExplanationUrl;
		private QuestionAnswer answer;
		private double positiveMark = 0;
		private double negativeMark = 0;
		private double skipMark = 0;
		/* metadata */
		private String fileName;
		private Map<String, String> usedInPapers;
		private List<UsedInTestDetails> usedInTestDetails;
		/* migration fields */
		Map<String, Object> migration;
		private String createdBy;
		private Date createdOn;
		private String lastUpdatedBy;
		private Date lastUpdatedOn;
	}

	@Data
	public static class UsedInTestDetails {
		private String testId;
		private String status;
		private String Name;
		private String tags;

		public UsedInTestDetails(String testId, String status, String name) {
			super();
			this.testId = testId;
			this.status = status;
			Name = name;
		}

	}
}
